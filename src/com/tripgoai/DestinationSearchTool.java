
package com.tripgoai;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DestinationSearchTool implements TravelTool {

    private static final String API_URL =
            "https://nominatim.openstreetmap.org/search";

    private static final HttpClient CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

    private static final Map<String, String> CACHE =
            new HashMap<>();

    private static long lastRequestTime = 0;

    @Override
    public String getName() {
        return "search";
    }

    @Override
    public synchronized String execute(String destination) {

        if (destination == null || destination.isBlank()) {
            return "Please provide a destination.";
        }

        String cleanedDestination = destination.trim();

        String cacheKey =
                cleanedDestination.toLowerCase(Locale.ROOT);

        if (CACHE.containsKey(cacheKey)) {
            return CACHE.get(cacheKey);
        }

        try {
            String result = fetchDestination(
                    cleanedDestination);

            if (result.startsWith(
                    "Destination Information:")) {

                CACHE.put(cacheKey, result);
            }

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Destination lookup interrupted.";

        } catch (IOException e) {
            return "Network error during destination lookup: "
                    + e.getMessage();

        } catch (IllegalArgumentException e) {
            return "Invalid destination: "
                    + e.getMessage();
        }
    }

    private String fetchDestination(String destination)
            throws IOException, InterruptedException {

        long elapsed =
                System.currentTimeMillis() - lastRequestTime;

        if (elapsed < 1100) {
            Thread.sleep(1100 - elapsed);
        }

        String encodedDestination =
                URLEncoder.encode(
                        destination,
                        StandardCharsets.UTF_8);

        String url = API_URL
                + "?q=" + encodedDestination
                + "&format=jsonv2"
                + "&addressdetails=1"
                + "&limit=1";

        // Set TRIPGOAI_CONTACT to your genuine project email
        // before using the public Nominatim service.
        String contact = System.getenv("TRIPGOAI_CONTACT");

        if (contact == null || contact.isBlank()) {
            return "Destination lookup is not configured. "
                    + "Set TRIPGOAI_CONTACT to your project "
                    + "contact email.";
        }

        String userAgent =
                "TripGoAI/1.0 (educational travel planner; "
                + contact.trim() + ")";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", userAgent)
                .header("Accept-Language", "en")
                .timeout(Duration.ofSeconds(25))
                .GET()
                .build();

        lastRequestTime = System.currentTimeMillis();

        HttpResponse<String> response = CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            return "Destination lookup failed (HTTP "
                    + response.statusCode() + ").";
        }

        String body = response.body().trim();

        if (body.equals("[]")) {
            return "No destination found for: "
                    + destination;
        }

        String name =
                getJsonValue(body, "display_name");

        String latitude =
                getJsonValue(body, "lat");

        String longitude =
                getJsonValue(body, "lon");

        String type =
                getJsonValue(body, "type");

        String country =
                getJsonValue(body, "country");

        if (name == null
                || latitude == null
                || longitude == null) {

            return "Unable to read destination data.";
        }

        StringBuilder result = new StringBuilder();

        result.append("Destination Information:\n");
        result.append("Name: ").append(name).append("\n");

        result.append("Country: ")
                .append(country == null
                        ? "Unknown" : country)
                .append("\n");

        result.append("Location Type: ")
                .append(type == null
                        ? "Unknown" : type)
                .append("\n");

        result.append("Latitude: ")
                .append(latitude).append("\n");

        result.append("Longitude: ")
                .append(longitude).append("\n");

        result.append("Source: OpenStreetMap contributors");

        return result.toString();
    }

    private String getJsonValue(String json, String key) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return null;
        }

        return unescapeJson(matcher.group(1));
    }

    private String unescapeJson(String text) {

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {

            char c = text.charAt(i);

            if (c != '\\' || i + 1 >= text.length()) {
                result.append(c);
                continue;
            }

            char next = text.charAt(++i);

            switch (next) {
                case '"':
                    result.append('"');
                    break;

                case '\\':
                    result.append('\\');
                    break;

                case '/':
                    result.append('/');
                    break;

                case 'n':
                    result.append('\n');
                    break;

                case 'r':
                    result.append('\r');
                    break;

                case 't':
                    result.append('\t');
                    break;

                case 'u':
                    if (i + 4 < text.length()) {
                        String hex =
                                text.substring(i + 1, i + 5);

                        try {
                            result.append((char)
                                    Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (NumberFormatException e) {
                            result.append("\\u");
                        }
                    } else {
                        result.append("\\u");
                    }
                    break;

                default:
                    result.append(next);
            }
        }

        return result.toString();
    }
}
