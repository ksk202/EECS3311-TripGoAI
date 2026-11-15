
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccommodationSearchTool implements TravelTool {

    private static final String NOMINATIM =
            "https://nominatim.openstreetmap.org/search";

    private static final String GEOCODING =
            "https://geocoding-api.open-meteo.com/v1/search";

    private static final HttpClient CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

    private static final long CACHE_MS =
            Duration.ofHours(6).toMillis();

    private static final Map<String, CacheEntry> CACHE =
            new ConcurrentHashMap<>();

    private static long lastNominatimRequest = 0;

    private static class AccommodationResult {
        final String name;
        final String type;

        AccommodationResult(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    private static class CacheEntry {
        final List<AccommodationResult> results;
        final long expiresAt;

        CacheEntry(List<AccommodationResult> results) {
            this.results = List.copyOf(results);
            this.expiresAt =
                    System.currentTimeMillis() + CACHE_MS;
        }
    }

    @Override
    public String getName() {
        return "accommodation";
    }

    @Override
    public String execute(String destination) {

        if (destination == null || destination.isBlank()) {
            return "Please provide a destination.";
        }

        try {
            List<AccommodationResult> results =
                    fetchAccommodations(destination);

            if (results.isEmpty()) {
                return "No accommodation listings found for "
                        + destination + ".";
            }

            StringBuilder output = new StringBuilder();

            output.append("Accommodation Options in ")
                    .append(destination)
                    .append(":\n\n");

            int number = 1;

            for (AccommodationResult result : results) {

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
                    + "Prices, availability, ratings, "
                    + "and booking eligibility are not verified.");

            return output.toString();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Accommodation request interrupted.";

        } catch (IOException | RuntimeException e) {
            return "Unable to retrieve accommodation: "
                    + e.getMessage();
        }
    }

    public List<Activity> getActivityCandidates(
            String destination) {

        try {
            List<AccommodationResult> results =
                    fetchAccommodations(destination);

            List<Activity> activities = new ArrayList<>();
            int id = 1;

            for (AccommodationResult result : results) {

                activities.add(new Activity(
                        id++,
                        "Stay at " + result.name,
                        destination.trim(),
                        null,
                        null,
                        0,
                        Double.NaN,
                        "Accommodation",
                        false));
            }

            return activities;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Accommodation lookup interrupted.", e);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Accommodation lookup failed: "
                    + e.getMessage(), e);
        }
    }

    private List<AccommodationResult> fetchAccommodations(
            String destination)
            throws IOException, InterruptedException {

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Destination cannot be empty.");
        }

        String key =
                destination.trim().toLowerCase(Locale.ROOT);

        CacheEntry cached = CACHE.get(key);

        if (cached != null
                && System.currentTimeMillis()
                        < cached.expiresAt) {

            System.out.println(
                    "TripGoAI: Using cached accommodation.");

            return new ArrayList<>(cached.results);
        }

        double[] coordinates = geocode(destination.trim());

        double lat = coordinates[0];
        double lon = coordinates[1];

        String viewbox =
                (lon - 0.035) + ","
                + (lat + 0.025) + ","
                + (lon + 0.035) + ","
                + (lat - 0.025);

        // Nominatim category search for hotels.
        String url = NOMINATIM
                + "?q=" + encode("[hotel]")
                + "&viewbox=" + encode(viewbox)
                + "&bounded=1"
                + "&format=jsonv2"
                + "&limit=30"
                + "&addressdetails=0";

        HttpResponse<String> response =
                requestNominatim(url);

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Accommodation service returned HTTP "
                    + response.statusCode());
        }

        List<AccommodationResult> results =
                parseAccommodations(response.body());

        CACHE.put(key, new CacheEntry(results));

        System.out.println(
                "TripGoAI: Nominatim accommodation search "
                + "returned " + results.size()
                + " results.");

        return results;
    }

    private double[] geocode(String destination)
            throws IOException, InterruptedException {

        String url = GEOCODING
                + "?name=" + encode(destination)
                + "&count=1"
                + "&language=en"
                + "&format=json";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(12))
                        .header("Accept", "application/json")
                        .GET()
                        .build();

        HttpResponse<String> response =
                CLIENT.send(
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
                System.currentTimeMillis()
                        - lastNominatimRequest;

        if (elapsed < 1100) {
            Thread.sleep(1100 - elapsed);
        }

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(20))
                        .header("Accept", "application/json")
                        .header(
                                "User-Agent",
                                "TripGoAI/1.0 (educational project; "
                                + "contact: " + contact.trim() + ")")
                        .GET()
                        .build();

        lastNominatimRequest =
                System.currentTimeMillis();

        return CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    private List<AccommodationResult> parseAccommodations(
            String json) {

        List<AccommodationResult> results =
                new ArrayList<>();

        Pattern objectPattern =
                Pattern.compile("\\{[^{}]*\\}");

        Matcher objects =
                objectPattern.matcher(json);

        while (objects.find() && results.size() < 10) {

            String item = objects.group();

            String category =
                    extractString(item, "category");

            if (category == null) {
                category = extractString(item, "class");
            }

            String type = extractString(item, "type");

            if (!"tourism".equals(category)
                    || type == null
                    || !List.of(
                            "hotel",
                            "hostel",
                            "guest_house",
                            "motel").contains(type)) {
                continue;
            }

            String name = extractString(item, "name");

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

            for (AccommodationResult existing : results) {
                if (existing.name.equalsIgnoreCase(name)) {
                    duplicate = true;
                    break;
                }
            }

            if (!duplicate) {
                results.add(new AccommodationResult(
                        name,
                        formatType(type)));
            }
        }

        return results;
    }

    private String formatType(String type) {

        switch (type) {
            case "hotel":
                return "Hotel";
            case "hostel":
                return "Hostel";
            case "guest_house":
                return "Guest House";
            case "motel":
                return "Motel";
            default:
                return type;
        }
    }

    private String extractString(
            String json, String key) {

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
            String json, String key) {

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
}
