
package com.tripgoai;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttractionSearchTool implements TravelTool {

    private static final String GEOCODING_URL =
            "https://geocoding-api.open-meteo.com/v1/search";

    private static final String WIKIPEDIA_URL =
            "https://en.wikipedia.org/w/api.php";

    private static final int MAX_RESULTS = 50;

    private static final long CACHE_MS =
            Duration.ofHours(6).toMillis();

    private static final HttpClient CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

    private static final Map<String, CacheEntry> CACHE =
            new ConcurrentHashMap<>();

    // Quality filtering settings.
    private static final int MIN_QUALITY_SCORE = 8;

    private static final Set<String> LOW_PRIORITY_SUBSITES =
            Set.of(
                    "basilica ulpia",
                    "basilica aemilia",
                    "basilica porcia",
                    "basilica opimia",
                    "basilica fulvia",
                    "palace of domitian",
                    "five columns monument",
                    "monument to cola di rienzo",
                    "monument to the porta pia breach"
            );

    private static class AttractionResult {

        final String name;
        final double distanceMeters;
        final int score;

        AttractionResult(
                String name,
                double distanceMeters,
                int score) {

            this.name = name;
            this.distanceMeters = distanceMeters;
            this.score = score;
        }
    }

    private static class CacheEntry {

        final List<AttractionResult> results;
        final long expiresAt;

        CacheEntry(List<AttractionResult> results) {

            this.results = List.copyOf(results);
            this.expiresAt =
                    System.currentTimeMillis() + CACHE_MS;
        }
    }

    @Override
    public String getName() {
        return "attractions";
    }

    @Override
    public String execute(String destination) {

        if (destination == null || destination.isBlank()) {
            return "Please provide a destination.";
        }

        try {

            List<AttractionResult> results =
                    fetchAttractions(destination);

            if (results.isEmpty()) {
                return "No nearby attraction suggestions "
                        + "found for " + destination + ".";
            }

            StringBuilder output = new StringBuilder();

            output.append("Attraction Suggestions for ")
                    .append(destination)
                    .append(":\n\n");

            int number = 1;

            for (AttractionResult result : results) {

                output.append(number++)
                        .append(". ")
                        .append(result.name)
                        .append("\n");
            }

            output.append(
                    "\nSource: Wikipedia geographic search.\n"
                    + "Tourist relevance is estimated. "
                    + "Admission prices, public access, and "
                    + "opening hours are not independently verified.");

            return output.toString();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            return "Attraction lookup interrupted.";

        } catch (IOException | RuntimeException e) {

            return "Attraction lookup unavailable: "
                    + e.getMessage();
        }
    }

    // Original Wikipedia attraction retrieval.
    private List<AttractionResult> fetchAttractions(
            String destination)
            throws IOException, InterruptedException {

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Destination cannot be empty.");
        }

        String city = destination.trim();

        String key = city.toLowerCase(Locale.ROOT);

        CacheEntry cached = CACHE.get(key);

        if (cached != null
                && System.currentTimeMillis()
                        < cached.expiresAt) {

            System.out.println(
                    "TripGoAI: Using cached attractions.");

            return new ArrayList<>(cached.results);
        }

        double[] coordinates = geocode(city);

        String location =
                coordinates[0] + "|" + coordinates[1];

        String url = WIKIPEDIA_URL
                + "?action=query"
                + "&list=geosearch"
                + "&gscoord=" + encode(location)
                + "&gsradius=10000"
                + "&gslimit=500"
                + "&format=json";

        String json = request(url);

        List<AttractionResult> results =
                parseAndRank(json);

        if (results.size() > MAX_RESULTS) {
            results = new ArrayList<>(
                    results.subList(0, MAX_RESULTS));
        }

        CACHE.put(key, new CacheEntry(results));

        System.out.println(
                "TripGoAI: Geographically filtered "
                + "attractions returned "
                + results.size() + " results.");

        return results;
    }

    private List<AttractionResult> parseAndRank(
            String json) {

        Map<String, AttractionResult> unique =
                new LinkedHashMap<>();

        Matcher section = Pattern.compile(
                "\"geosearch\"\\s*:\\s*\\[(.*?)\\]",
                Pattern.DOTALL)
                .matcher(json);

        if (!section.find()) {
            return new ArrayList<>();
        }

        Matcher objects = Pattern.compile(
                "\\{[^{}]*\\}")
                .matcher(section.group(1));

        while (objects.find()) {

            String item = objects.group();

            String name =
                    extractString(item, "title");

            Double distance =
                    extractNumber(item, "dist");

            if (name == null
                    || name.isBlank()
                    || distance == null
                    || distance > 10000) {
                continue;
            }

            name = name.trim();

            if (!isRelevant(name)) {
                continue;
            }

            int score = calculateScore(name);

            if (score <= 0) {
                continue;
            }

            String nameKey = normalizeName(name);

            unique.putIfAbsent(
                    nameKey,
                    new AttractionResult(
                            name, distance, score));
        }

        List<AttractionResult> results =
                new ArrayList<>(unique.values());

        results.sort(
                Comparator.comparingInt(
                        (AttractionResult a) -> a.score)
                        .reversed()
                        .thenComparingDouble(
                                a -> a.distanceMeters));

        return results;
    }

    private boolean isRelevant(String name) {

        String lower =
                name.toLowerCase(Locale.ROOT);

        String[] excluded = {
                "metro station",
                "railway station",
                "bus station",
                "tram stop",
                "airport",
                "football club",
                "football team",
                "disambiguation",
                "list of",
                "municipality",
                "administrative",
                "political party",
                "railway line",
                "transport in",
                "history of",
                "shopping mall",
                "election",
                "politician",
                "demolished building"
        };

        for (String word : excluded) {
            if (lower.contains(word)) {
                return false;
            }
        }

        String[] generic = {
                "landmark",
                "monument",
                "museum",
                "tourism",
                "attraction",
                "architecture"
        };

        for (String word : generic) {
            if (lower.equals(word)) {
                return false;
            }
        }

        return true;
    }

    private int calculateScore(String name) {

        String lower =
                name.toLowerCase(Locale.ROOT);

        int score = 0;

        String[] recognizableTypes = {
                "museum",
                "gallery",
                "cathedral",
                "basilica",
                "palace",
                "castle",
                "monument",
                "fountain",
                "tower",
                "amphitheatre",
                "amphitheater",
                "fortress",
                "aquarium",
                "zoo"
        };

        for (String word : recognizableTypes) {
            if (lower.contains(word)) {
                score += 8;
            }
        }

        String[] otherAttractions = {
                "park",
                "garden",
                "piazza",
                "square",
                "bridge",
                "temple",
                "church",
                "shrine",
                "mosque",
                "pagoda",
                "palazzo",
                "villa",
                "forum",
                "historic",
                "archaeological"
        };

        for (String word : otherAttractions) {
            if (lower.contains(word)) {
                score += 3;
            }
        }

        String[] lowerPriority = {
                "fragment",
                "remains",
                "demolished",
                "lost building"
        };

        for (String word : lowerPriority) {
            if (lower.contains(word)) {
                score -= 12;
            }
        }

        if (score == 0) {
            score = 1;
        }

        return score;
    }

    private double[] geocode(String destination)
            throws IOException, InterruptedException {

        String url = GEOCODING_URL
                + "?name=" + encode(destination)
                + "&count=1"
                + "&language=en"
                + "&format=json";

        String json = request(url);

        Double latitude =
                extractNumber(json, "latitude");

        Double longitude =
                extractNumber(json, "longitude");

        if (latitude == null || longitude == null) {
            throw new IOException(
                    "Destination coordinates unavailable: "
                    + destination);
        }

        return new double[]{latitude, longitude};
    }

    private String request(String url)
            throws IOException, InterruptedException {

        String contact =
                System.getenv("TRIPGOAI_CONTACT");

        String userAgent =
                "TripGoAI/1.0 (educational travel planner";

        if (contact != null && !contact.isBlank()) {
            userAgent += "; contact: " + contact.trim();
        }

        userAgent += ")";

        HttpRequest httpRequest =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(20))
                        .header(
                                "Accept", "application/json")
                        .header(
                                "User-Agent", userAgent)
                        .GET()
                        .build();

        HttpResponse<String> response =
                CLIENT.send(
                        httpRequest,
                        HttpResponse.BodyHandlers.ofString(
                                StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new IOException(
                    "API returned HTTP "
                    + response.statusCode());
        }

        return response.body();
    }

    private String extractString(
            String json,
            String key) {

        Matcher matcher = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*"
                + "\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(json);

        if (!matcher.find()) {
            return null;
        }

        return decodeJson(matcher.group(1));
    }

    private Double extractNumber(
            String json,
            String key) {

        Matcher matcher = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
                .matcher(json);

        return matcher.find()
                ? Double.parseDouble(matcher.group(1))
                : null;
    }

    private String decodeJson(String value) {

        StringBuilder result =
                new StringBuilder();

        for (int i = 0; i < value.length(); i++) {

            char c = value.charAt(i);

            if (c != '\\' || i + 1 >= value.length()) {
                result.append(c);
                continue;
            }

            char next = value.charAt(++i);

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

                case 't':
                    result.append('\t');
                    break;

                case 'r':
                    result.append('\r');
                    break;

                case 'u':

                    if (i + 4 < value.length()) {

                        try {
                            String hex =
                                    value.substring(
                                            i + 1, i + 5);

                            result.append(
                                    (char) Integer.parseInt(
                                            hex, 16));

                            i += 4;

                        } catch (NumberFormatException e) {
                            result.append("\\u");
                        }
                    }
                    break;

                default:
                    result.append(next);
            }
        }

        return result.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value, StandardCharsets.UTF_8);
    }

    private String normalizeName(String name) {

        return name.toLowerCase(Locale.ROOT)
                .replaceAll("\\([^)]*\\)", " ")
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isOutdoorAttraction(String name) {

        if (name == null || name.isBlank()) {
            return false;
        }

        String lower = name.toLowerCase(Locale.ROOT);

        String[] indoor = {
                "museum",
                "gallery",
                "theatre",
                "theater",
                "library",
                "aquarium",
                "exhibition"
        };

        for (String word : indoor) {
            if (lower.contains(word)) {
                return false;
            }
        }

        String[] outdoor = {
                "colosseum",
                "roman forum",
                "palatine hill",
                "trevi fountain",
                "spanish steps",
                "piazza",
                "square",
                "garden",
                "park",
                "beach",
                "fountain",
                "bridge",
                "amphitheatre"
        };

        for (String word : outdoor) {
            if (lower.contains(word)) {
                return true;
            }
        }

        return false;
    }

    // F05 + F09: Preserve documented attraction pricing.
    public List<Activity> getActivityCandidates(
            String destination) {

        try {
            List<AttractionResult> results =
                    fetchAttractions(destination);

            AttractionPriceService priceService =
                    new AttractionPriceService();

            List<Activity> activities =
                    new ArrayList<>();

            int id = 1;
            int pricedCount = 0;

            for (AttractionResult result : results) {

                double estimatedCost =
                        priceService.findPrice(
                                destination,
                                result.name);

                if (Double.isFinite(estimatedCost)) {
                    pricedCount++;
                }

                activities.add(new Activity(
                        id++,
                        result.name,
                        destination.trim(),
                        null,
                        null,
                        0,
                        estimatedCost,
                        "Attraction",
                        isOutdoorAttraction(result.name)));
            }

            System.out.println(
                    "TripGoAI: Documented prices available "
                    + "for " + pricedCount
                    + " of " + activities.size()
                    + " attraction candidates.");

            return activities;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Attraction lookup interrupted.", e);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Attraction lookup failed: "
                    + e.getMessage(), e);
        }
    }

    // ==================================================
    // MERGED ATTRACTION QUALITY FILTER
    // Previously AttractionQualityFilter.java
    // ==================================================

    public static List<Activity> filterCandidates(
            List<Activity> candidates) {

        if (candidates == null) {
            throw new IllegalArgumentException(
                    "Attraction candidates cannot be null.");
        }

        List<Activity> filtered = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Activity activity : candidates) {

            if (activity == null
                    || activity.getName() == null
                    || activity.getName().isBlank()) {
                continue;
            }

            String name = qualityNormalize(
                    activity.getName());

            if (name.isEmpty()) {
                continue;
            }

            if (isClearlyUnsuitable(activity)) {
                continue;
            }

            if (qualityScore(activity)
                    < MIN_QUALITY_SCORE) {
                continue;
            }

            if (!seen.add(name)) {
                continue;
            }

            filtered.add(activity);
        }

        filtered.sort(
                Comparator
                        .comparingInt(
                                AttractionSearchTool::qualityScore)
                        .reversed()
                        .thenComparing(
                                activity -> qualityNormalize(
                                        activity.getName())));

        return filtered;
    }

    private static String qualityNormalize(String text) {

        if (text == null) {
            return "";
        }

        String cleaned = Normalizer.normalize(
                text, Normalizer.Form.NFD);

        return cleaned
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\([^)]*\\)", " ")
                .replaceAll(
                        "\\[ai suggestion[^\\]]*\\]", " ")
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean qualityContainsAny(
            String normalizedText,
            String... phrases) {

        String padded = " " + normalizedText + " ";

        for (String phrase : phrases) {

            String normalizedPhrase =
                    qualityNormalize(phrase);

            if (!normalizedPhrase.isEmpty()
                    && padded.contains(
                            " " + normalizedPhrase + " ")) {
                return true;
            }
        }

        return false;
    }

    private static boolean isClearlyUnsuitable(
            Activity activity) {

        String name = qualityNormalize(activity.getName());

        if (qualityContainsAny(
                name,
                "disambiguation",
                "list of",
                "administrative division",
                "political party",
                "football club",
                "private residence",
                "private property",
                "industrial estate",
                "office building",
                "municipal office",
                "demolished building",
                "destroyed building",
                "lost building",
                "no longer exists",
                "former railway",
                "abandoned factory")) {

            return true;
        }

        boolean transportFacility = qualityContainsAny(
                name,
                "railway station",
                "train station",
                "metro station",
                "subway station",
                "bus station",
                "airport",
                "parking garage");

        boolean museumOrHeritage = qualityContainsAny(
                name,
                "museum",
                "museums",
                "gallery",
                "heritage",
                "historic");

        return transportFacility && !museumOrHeritage;
    }

    private static int qualityScore(Activity activity) {

        String name = qualityNormalize(activity.getName());

        String category = qualityNormalize(
                activity.getCategory());

        int score = 0;

        if (qualityContainsAny(
                name,
                "colosseum",
                "roman forum",
                "pantheon",
                "vatican museums",
                "st peters basilica",
                "st peter s basilica",
                "saint peter s basilica",
                "trevi fountain",
                "spanish steps",
                "capitoline museums",
                "palatine hill",
                "castel sant angelo",
                "piazza navona",
                "sistine chapel",
                "victor emmanuel ii monument",
                "eiffel tower",
                "louvre",
                "tower of london",
                "british museum",
                "senso ji",
                "tokyo tower",
                "sagrada familia",
                "great wall of china",
                "statue of liberty",
                "niagara falls")) {

            score += 35;
        }

        if (qualityContainsAny(
                name,
                "museum",
                "museums",
                "gallery",
                "cathedral",
                "archbasilica",
                "aquarium",
                "zoo",
                "science centre",
                "science center",
                "art centre",
                "art center")) {

            score += 16;
        }

        if (qualityContainsAny(
                name,
                "basilica",
                "palace",
                "castle",
                "fortress",
                "shrine",
                "temple",
                "amphitheatre",
                "amphitheater",
                "heritage site")) {

            score += 12;
        }

        if (qualityContainsAny(
                name,
                "monument",
                "fountain",
                "tower",
                "garden",
                "park",
                "piazza",
                "square",
                "bridge",
                "waterfall",
                "falls",
                "beach",
                "viewpoint",
                "observation deck",
                "national park",
                "botanical garden",
                "archaeological park",
                "historic site",
                "market",
                "crossing")) {

            score += 8;
        }

        if (qualityContainsAny(
                name,
                "church",
                "chapel",
                "ruins",
                "archaeological",
                "historical",
                "landmark",
                "villa",
                "trail",
                "mountain",
                "lake")) {

            score += 4;
        }

        if (qualityContainsAny(
                category,
                "museum",
                "gallery",
                "heritage",
                "tourism",
                "historic",
                "cultural")) {

            score += 3;
        }

        if (qualityContainsAny(
                name,
                "fragment",
                "remains of",
                "foundations of",
                "lost building",
                "excavation",
                "archaeological remains",
                "nymphaeum")) {

            score -= 20;
        }

        if (LOW_PRIORITY_SUBSITES.contains(name)) {
            score -= 25;
        }

        if (name.length() > 90) {
            score -= 3;
        }

        return score;
    }
}
