
package com.tripgoai;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransportationTool implements TravelTool {

    private static final String NOMINATIM =
            "https://nominatim.openstreetmap.org/search";

    private static final String GEOCODING =
            "https://geocoding-api.open-meteo.com/v1/search";

    private static final String OSRM_URL =
            "https://router.project-osrm.org/route/v1/driving/";

    private static final HttpClient CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

    private static final HttpClient ROUTE_CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

    private static final long CACHE_MS =
            Duration.ofHours(6).toMillis();

    private static final Map<String, CacheEntry> CACHE =
            new ConcurrentHashMap<>();

    private static long lastRequest = 0;

    private static class TransportResult {

        final String name;
        final String type;

        TransportResult(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    private static class CacheEntry {

        final List<TransportResult> results;
        final long expiresAt;

        CacheEntry(List<TransportResult> results) {
            this.results = List.copyOf(results);
            this.expiresAt =
                    System.currentTimeMillis() + CACHE_MS;
        }
    }

    // Routing data, previously in TransportRoutingService.
    private static final class Coordinates {

        final double latitude;
        final double longitude;
        final String query;
        final String displayName;

        Coordinates(
                double latitude,
                double longitude,
                String query,
                String displayName) {

            this.latitude = latitude;
            this.longitude = longitude;
            this.query = query;
            this.displayName = displayName;
        }
    }

    @Override
    public String getName() {
        return "transport";
    }

    // Existing public transportation search.
    @Override
    public String execute(String destination) {

        if (destination == null || destination.isBlank()) {
            return "Please provide a destination.";
        }

        try {
            List<TransportResult> results =
                    fetchTransport(destination);

            if (results.isEmpty()) {
                return "No matching public transport stations "
                        + "found near " + destination + ".";
            }

            StringBuilder output = new StringBuilder();

            output.append("Public Transportation Near ")
                    .append(destination)
                    .append(":\n\n");

            int number = 1;

            for (TransportResult result : results) {

                output.append(number++)
                        .append(". ")
                        .append(result.name)
                        .append("\n   Type: ")
                        .append(result.type)
                        .append("\n\n");
            }

            output.append(
                    "Source: OpenStreetMap contributors "
                    + "(Nominatim search).\n"
                    + "Results are not comprehensive. "
                    + "Routes, fares, schedules and "
                    + "service availability are not verified.");

            return output.toString();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            return "Transportation request interrupted.";

        } catch (IOException | RuntimeException e) {

            return "Unable to retrieve transportation: "
                    + e.getMessage();
        }
    }

    // Existing transport activity recommendations.
    public List<Activity> getActivityCandidates(
            String destination) {

        try {
            List<TransportResult> results =
                    fetchTransport(destination);

            List<Activity> activities = new ArrayList<>();
            int id = 1;

            for (TransportResult result : results) {

                activities.add(new Activity(
                        id++,
                        "Transport via " + result.name,
                        destination.trim(),
                        null,
                        null,
                        0,
                        Double.NaN,
                        "Transport",
                        false));
            }

            return activities;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Transport request interrupted.", e);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Transport lookup failed: "
                    + e.getMessage(), e);
        }
    }

    // F08 - Driving route planning.
    // Merged from TransportRoutingService.
    public String planDrivingRoute(
            String origin,
            String destination) {

        if (origin == null || origin.isBlank()) {
            throw new IllegalArgumentException(
                    "Starting location is required.");
        }

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Destination is required.");
        }

        origin = origin.trim();
        destination = destination.trim();

        Coordinates start = locate(origin);
        Coordinates end = locate(destination);

        String coordinates = String.format(
                Locale.US,
                "%.7f,%.7f;%.7f,%.7f",
                start.longitude,
                start.latitude,
                end.longitude,
                end.latitude);

        String url = OSRM_URL + coordinates
                + "?overview=false&steps=false";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "TripGoAI/1.0")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        try {

            HttpResponse<String> response =
                    ROUTE_CLIENT.send(
                            request,
                            HttpResponse.BodyHandlers.ofString(
                                    StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Routing service returned HTTP "
                        + response.statusCode());
            }

            String json = response.body();

            if (!Pattern.compile(
                    "\"code\"\\s*:\\s*\"Ok\"")
                    .matcher(json).find()) {

                throw new IllegalStateException(
                        "No driving route was found.");
            }

            Matcher routes = Pattern.compile(
                    "\"routes\"\\s*:\\s*\\[\\s*\\{")
                    .matcher(json);

            if (!routes.find()) {
                throw new IllegalStateException(
                        "No route data was returned.");
            }

            String routeData =
                    json.substring(routes.end());

            double distanceMeters =
                    readRouteNumber(routeData, "distance");

            double durationSeconds =
                    readRouteNumber(routeData, "duration");

            if (!Double.isFinite(distanceMeters)
                    || !Double.isFinite(durationSeconds)
                    || distanceMeters < 0
                    || durationSeconds < 0) {

                throw new IllegalStateException(
                        "Invalid route measurements.");
            }

            double kilometers =
                    distanceMeters / 1000.0;

            long minutes =
                    (long) Math.ceil(
                            durationSeconds / 60.0);

            String mapCoordinates = String.format(
                    Locale.US,
                    "%.7f,%.7f;%.7f,%.7f",
                    start.latitude,
                    start.longitude,
                    end.latitude,
                    end.longitude);

            String mapUrl =
                    "https://www.openstreetmap.org/directions"
                    + "?engine=fossgis_osrm_car&route="
                    + URLEncoder.encode(
                            mapCoordinates,
                            StandardCharsets.UTF_8);

            StringBuilder result = new StringBuilder();

            result.append(
                    "=== TRANSPORTATION ROUTE ===\n\n");

            result.append("From: ")
                    .append(origin)
                    .append("\n");

            result.append("To: ")
                    .append(destination)
                    .append("\n");

            result.append("Mode: Driving\n\n");

            result.append(
                    "=== RESOLVED LOCATIONS ===\n");

            result.append("Starting point: ")
                    .append(start.displayName)
                    .append("\n");

            result.append("Destination point: ")
                    .append(end.displayName)
                    .append("\n");

            if (!start.query.equalsIgnoreCase(origin)
                    || !end.query.equalsIgnoreCase(
                            destination)) {

                result.append(
                        "\nNote: A shortened location "
                        + "query was used. Verify the "
                        + "resolved map endpoints.\n");
            }

            result.append(
                    "\n=== ROUTE ESTIMATE ===\n");

            result.append(String.format(
                    Locale.US,
                    "Distance: %.1f km%n",
                    kilometers));

            result.append(String.format(
                    Locale.US,
                    "Travel Time: %d h %02d min%n",
                    minutes / 60,
                    minutes % 60));

            result.append(
                    "\n=== TRANSPORT COST ===\n"
                    + "Fuel: Unknown\n"
                    + "Tolls: Unknown\n"
                    + "Parking: Unknown\n"
                    + "Total: Unconfirmed\n");

            result.append("\n=== MAP ===\n");
            result.append(mapUrl).append("\n");

            result.append(
                    "\nSource: OSRM and OpenStreetMap "
                    + "contributors.\n");

            result.append(
                    "Route duration is estimated. "
                    + "Live traffic is not included.\n");

            result.append(
                    "Opening hours, parking, tolls "
                    + "and public transport services "
                    + "are not verified.");

            return result.toString();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to retrieve the driving route.",
                    e);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Routing request interrupted.", e);
        }
    }

    // Existing transport station retrieval.
    private List<TransportResult> fetchTransport(
            String destination)
            throws IOException, InterruptedException {

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Destination cannot be empty.");
        }

        String key = destination.trim()
                .toLowerCase(Locale.ROOT);

        CacheEntry cached = CACHE.get(key);

        if (cached != null
                && System.currentTimeMillis()
                        < cached.expiresAt) {

            System.out.println(
                    "TripGoAI: Using cached transportation.");

            return new ArrayList<>(cached.results);
        }

        double[] coordinates =
                geocode(destination.trim());

        double lat = coordinates[0];
        double lon = coordinates[1];

        String viewbox =
                (lon - 0.045) + ","
                + (lat + 0.035) + ","
                + (lon + 0.045) + ","
                + (lat - 0.035);

        String url = NOMINATIM
                + "?q=" + encode("[station]")
                + "&viewbox=" + encode(viewbox)
                + "&bounded=1"
                + "&format=jsonv2"
                + "&limit=40"
                + "&addressdetails=0";

        HttpResponse<String> response =
                requestNominatim(url);

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Transport search returned HTTP "
                    + response.statusCode());
        }

        List<TransportResult> results =
                parseTransport(response.body());

        CACHE.put(key, new CacheEntry(results));

        System.out.println(
                "TripGoAI: Nominatim transport search "
                + "returned " + results.size()
                + " results.");

        return results;
    }

    // Existing Open-Meteo geocoding.
    private double[] geocode(String destination)
            throws IOException, InterruptedException {

        String url = GEOCODING
                + "?name=" + encode(destination)
                + "&count=1"
                + "&language=en"
                + "&format=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(12))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Geocoding returned HTTP "
                    + response.statusCode());
        }

        String json = response.body();

        Double lat = extractNumber(json, "latitude");
        Double lon = extractNumber(json, "longitude");

        if (lat == null || lon == null) {
            throw new IOException(
                    "Destination coordinates unavailable: "
                    + destination);
        }

        return new double[]{lat, lon};
    }

    private static synchronized HttpResponse<String>
            requestNominatim(String url)
            throws IOException, InterruptedException {

        String contact =
                System.getenv("TRIPGOAI_CONTACT");

        if (contact == null || contact.isBlank()) {
            throw new IOException(
                    "Set TRIPGOAI_CONTACT in Eclipse "
                    + "Run Configurations > Environment.");
        }

        long elapsed =
                System.currentTimeMillis() - lastRequest;

        if (elapsed < 1100) {
            Thread.sleep(1100 - elapsed);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header(
                        "User-Agent",
                        "TripGoAI/1.0 (educational project; "
                        + "contact: " + contact.trim() + ")")
                .GET()
                .build();

        lastRequest = System.currentTimeMillis();

        return CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    // Existing public-transport result parser.
    private List<TransportResult> parseTransport(
            String json) {

        List<TransportResult> results =
                new ArrayList<>();

        Pattern objectPattern =
                Pattern.compile("\\{[^{}]*\\}");

        Matcher objects =
                objectPattern.matcher(json);

        while (objects.find() && results.size() < 12) {

            String item = objects.group();

            String category =
                    extractString(item, "category");

            if (category == null) {
                category = extractString(item, "class");
            }

            String type =
                    extractString(item, "type");

            String transportType =
                    classifyTransport(category, type);

            if (transportType == null) {
                continue;
            }

            String name =
                    extractString(item, "name");

            if (name == null || name.isBlank()) {

                String display =
                        extractString(item, "display_name");

                if (display != null) {
                    name = display.split(",")[0].trim();
                }
            }

            if (name == null || name.isBlank()) {
                continue;
            }

            boolean duplicate = false;

            for (TransportResult existing : results) {

                if (existing.name.equalsIgnoreCase(name)
                        && existing.type.equals(transportType)) {

                    duplicate = true;
                    break;
                }
            }

            if (!duplicate) {
                results.add(new TransportResult(
                        name, transportType));
            }
        }

        return results;
    }

    private String classifyTransport(
            String category,
            String type) {

        if (category == null || type == null) {
            return null;
        }

        if ("railway".equals(category)) {

            switch (type) {

                case "station":
                    return "Railway / Metro Station";

                case "halt":
                    return "Railway Halt";

                case "tram_stop":
                    return "Tram Stop";

                default:
                    return null;
            }
        }

        if ("amenity".equals(category)
                && "bus_station".equals(type)) {
            return "Bus Station";
        }

        if ("public_transport".equals(category)
                && "station".equals(type)) {
            return "Public Transport Station";
        }

        return null;
    }

    private String extractString(
            String json,
            String key) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*"
                + "\"((?:\\\\.|[^\"\\\\])*)\"");

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private Double extractNumber(
            String json,
            String key) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

        Matcher matcher = pattern.matcher(json);

        return matcher.find()
                ? Double.parseDouble(matcher.group(1))
                : null;
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value, StandardCharsets.UTF_8);
    }

    // Merged routing location lookup.
    private Coordinates locate(String location) {

        DestinationSearchTool searchTool = new DestinationSearchTool();

        List<String> queries =
                buildSearchQueries(location);

        String lastResult = "";

        for (String query : queries) {

            String result = searchTool.execute(query);

            if (result == null) {
                throw new IllegalStateException(
                        "Location lookup returned no response.");
            }

            if (result.startsWith(
                    "Destination Information:")) {

                double latitude =
                        readLabel(result, "Latitude");

                double longitude =
                        readLabel(result, "Longitude");

                if (!Double.isFinite(latitude)
                        || !Double.isFinite(longitude)
                        || latitude < -90
                        || latitude > 90
                        || longitude < -180
                        || longitude > 180) {

                    throw new IllegalStateException(
                            "Invalid coordinates for "
                            + location);
                }

                String displayName =
                        readDisplayName(result);

                System.out.println(
                        "TripGoAI: Location resolved "
                        + "using: " + query);

                return new Coordinates(
                        latitude,
                        longitude,
                        query,
                        displayName);
            }

            lastResult = result;

            if (!result.startsWith(
                    "No destination found for:")) {

                throw new IllegalStateException(
                        "Unable to locate "
                        + location + ": " + result);
            }
        }

        throw new IllegalStateException(
                "Unable to locate " + location
                + " after trying shorter queries. "
                + "Try the landmark's official street "
                + "address or a different local name. "
                + "Last lookup: " + lastResult);
    }

    private List<String> buildSearchQueries(
            String location) {

        Set<String> unique = new LinkedHashSet<>();

        String cleaned = location.trim();
        unique.add(cleaned);

        String[] parts = cleaned.split(",");

        if (parts.length >= 3) {

            String withoutCountry =
                    parts[0].trim()
                    + ", " + parts[1].trim();

            unique.add(withoutCountry);
        }

        if (parts.length >= 2) {

            String landmarkName =
                    parts[0].trim();

            if (!landmarkName.isBlank()) {
                unique.add(landmarkName);
            }
        }

        return new ArrayList<>(unique);
    }

    private double readLabel(
            String text,
            String label) {

        Pattern pattern = Pattern.compile(
                "(?m)^" + Pattern.quote(label)
                + ":\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");

        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            throw new IllegalStateException(
                    "Missing location field: " + label);
        }

        return Double.parseDouble(matcher.group(1));
    }

    private String readDisplayName(String text) {

        Matcher matcher = Pattern.compile(
                "(?m)^Name:\\s*(.+)$")
                .matcher(text);

        return matcher.find()
                ? matcher.group(1).trim()
                : "Name unavailable";
    }

    private double readRouteNumber(
            String json,
            String key) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*(\\d+(?:\\.\\d+)?)");

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalStateException(
                    "Missing route measurement: " + key);
        }

        return Double.parseDouble(matcher.group(1));
    }
}
