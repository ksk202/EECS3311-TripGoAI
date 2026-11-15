
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

public class RestaurantSearchTool implements TravelTool {

    private static final String API =
            "https://nominatim.openstreetmap.org/search";

    private static final HttpClient CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

    private static final long CACHE_MS =
            Duration.ofHours(6).toMillis();

    private static final Map<String, CacheEntry> CACHE =
            new ConcurrentHashMap<>();

    // Shared rate limit within this Java process.
    private static long lastRequest = 0;

    private static class RestaurantResult {
        final String name;

        RestaurantResult(String name) {
            this.name = name;
        }
    }

    private static class CacheEntry {
        final List<RestaurantResult> results;
        final long expiresAt;

        CacheEntry(List<RestaurantResult> results) {
            this.results = List.copyOf(results);
            this.expiresAt =
                    System.currentTimeMillis() + CACHE_MS;
        }
    }

    @Override
    public String getName() {
        return "restaurants";
    }

    @Override
    public String execute(String destination) {

        try {
            List<RestaurantResult> restaurants =
                    fetchRestaurants(destination);

            if (restaurants.isEmpty()) {
                return "No restaurant matches found for "
                        + destination + ".";
            }

            StringBuilder output = new StringBuilder();

            output.append("Restaurant Suggestions for ")
                    .append(destination)
                    .append(":\n");

            int number = 1;

            for (RestaurantResult restaurant : restaurants) {
                output.append(number++)
                        .append(". ")
                        .append(restaurant.name)
                        .append("\n");
            }

            output.append(
                    "\nSource: OpenStreetMap contributors "
                    + "(Nominatim search)."
                    + "\nResults are not comprehensive. "
                    + "Prices and opening hours are unverified.");

            return output.toString();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Restaurant request interrupted.";

        } catch (IOException | RuntimeException e) {
            return "Unable to retrieve restaurants: "
                    + e.getMessage();
        }
    }

    public List<Activity> getActivityCandidates(
            String destination) {

        try {
            List<RestaurantResult> restaurants =
                    fetchRestaurants(destination);

            List<Activity> activities = new ArrayList<>();

            int id = 1;

            for (RestaurantResult restaurant : restaurants) {

                activities.add(new Activity(
                        id++,
                        "Eat at " + restaurant.name,
                        destination.trim(),
                        null,
                        null,
                        0,
                        Double.NaN,
                        "Restaurant",
                        false));
            }

            return activities;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Restaurant lookup interrupted.", e);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Restaurant lookup failed.", e);
        }
    }

    private List<RestaurantResult> fetchRestaurants(
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
                    "TripGoAI: Using cached restaurants.");

            return new ArrayList<>(cached.results);
        }

        // One bounded search, no Overpass dependency.
        // Geocoding via Open-Meteo is separate.
        double[] coords = geocode(destination.trim());

        double lat = coords[0];
        double lon = coords[1];

        double latitudeDelta = 0.025;
        double longitudeDelta = 0.035;

        String viewbox =
                (lon - longitudeDelta) + ","
                + (lat + latitudeDelta) + ","
                + (lon + longitudeDelta) + ","
                + (lat - latitudeDelta);

        String url = API
                + "?q=" + encode("[restaurant]")
                + "&viewbox=" + encode(viewbox)
                + "&bounded=1"
                + "&format=jsonv2"
                + "&limit=20"
                + "&addressdetails=0";

        HttpResponse<String> response = request(url);

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Nominatim returned HTTP "
                    + response.statusCode());
        }

        List<RestaurantResult> restaurants =
                parseRestaurants(response.body());

        CACHE.put(key, new CacheEntry(restaurants));

        System.out.println(
                "TripGoAI: Nominatim restaurant search "
                + "returned " + restaurants.size()
                + " results.");

        return restaurants;
    }

    private double[] geocode(String destination)
            throws IOException, InterruptedException {

        String url =
                "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + encode(destination)
                + "&count=1&language=en&format=json";

        HttpRequest request = HttpRequest.newBuilder()
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
                    "Destination coordinates unavailable.");
        }

        return new double[]{lat, lon};
    }

    private static synchronized HttpResponse<String> request(
            String url)
            throws IOException, InterruptedException {

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
                        + "contact via TRIPGOAI_CONTACT)")
                .GET()
                .build();

        lastRequest = System.currentTimeMillis();

        return CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    private List<RestaurantResult> parseRestaurants(
            String json) {

        List<RestaurantResult> results = new ArrayList<>();

        // Match individual flat Nominatim result objects.
        Pattern objectPattern =
                Pattern.compile("\\{[^{}]*\\}");

        Matcher objects = objectPattern.matcher(json);

        while (objects.find() && results.size() < 10) {

            String item = objects.group();

            String category = extractString(item, "category");
            if (category == null) {
                category = extractString(item, "class");
            }

            String type = extractString(item, "type");

            if (!"amenity".equals(category)
                    || !"restaurant".equals(type)) {
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

            if (name != null && !name.isBlank()) {
                boolean duplicate = false;

                for (RestaurantResult existing : results) {
                    if (existing.name.equalsIgnoreCase(name)) {
                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    results.add(new RestaurantResult(name));
                }
            }
        }

        return results;
    }

    private String extractString(String json, String key) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private Double extractNumber(String json, String key) {

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
