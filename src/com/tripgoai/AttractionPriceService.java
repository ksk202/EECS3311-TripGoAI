
package com.tripgoai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AttractionPriceService {

    private static final Path PRICE_FILE =
            Path.of("attraction-prices.tsv");

    private static final int MAX_AGE_DAYS = 90;

    private static final HttpClient CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(8))
                    .build();

    private final Map<String, PriceRecord> prices =
            new HashMap<>();

    private final Map<String, Double> exchangeRates =
            new HashMap<>();

    private final Map<String, PriceQuote> estimateCache =
            new HashMap<>();

    private final LLMClient llmClient;

    public enum PriceStatus {
        DOCUMENTED,
        AI_ESTIMATE,
        UNKNOWN
    }

    public static final class PriceQuote {

        private final double amountUsd;
        private final PriceStatus status;
        private final String source;
        private final String explanation;

        private PriceQuote(
                double amountUsd,
                PriceStatus status,
                String source,
                String explanation) {

            this.amountUsd = amountUsd;
            this.status = status;
            this.source = source;
            this.explanation = explanation;
        }

        public double getAmountUsd() {
            return amountUsd;
        }

        public PriceStatus getStatus() {
            return status;
        }

        public String getSource() {
            return source;
        }

        public String getExplanation() {
            return explanation;
        }

        public boolean hasAmount() {
            return Double.isFinite(amountUsd)
                    && amountUsd >= 0;
        }

        public boolean isDocumented() {
            return status == PriceStatus.DOCUMENTED;
        }

        public boolean isEstimate() {
            return status == PriceStatus.AI_ESTIMATE;
        }

        @Override
        public String toString() {

            if (!hasAmount()) {
                return "Cost unknown";
            }

            String label = isDocumented()
                    ? "Documented price"
                    : "AI admission estimate (unverified)";

            return String.format(
                    Locale.US,
                    "%s: $%.2f USD",
                    label,
                    amountUsd);
        }
    }

    private static final class PriceRecord {

        final double amount;
        final String currency;
        final String sourceURL;
        final LocalDate checkedDate;

        PriceRecord(
                double amount,
                String currency,
                String sourceURL,
                LocalDate checkedDate) {

            this.amount = amount;
            this.currency = currency;
            this.sourceURL = sourceURL;
            this.checkedDate = checkedDate;
        }
    }

    // Preserves compatibility with existing code.
    public AttractionPriceService() {
        this(null);
    }

    // Use this constructor for AI-assisted estimates.
    public AttractionPriceService(LLMClient llmClient) {
        this.llmClient = llmClient;
        loadPriceCatalog();
    }

    // Existing callers receive documented prices only.
    // An unknown price is never silently replaced by
    // an unverified AI estimate.
    public synchronized double findPrice(
            String destination,
            String attractionName) {

        PriceQuote quote =
                findDocumentedPrice(
                        destination,
                        attractionName);

        return quote.hasAmount()
                ? quote.getAmountUsd()
                : Double.NaN;
    }

    // New method: documented price first, AI estimate second.
    public synchronized PriceQuote researchPrice(
            String destination,
            String attractionName) {

        if (destination == null
                || attractionName == null
                || destination.isBlank()
                || attractionName.isBlank()) {

            return unknownQuote();
        }

        PriceQuote documented =
                findDocumentedPrice(
                        destination,
                        attractionName);

        if (documented.hasAmount()) {
            return documented;
        }

        if (llmClient == null) {
            return unknownQuote();
        }

        String key = makeKey(
                destination,
                attractionName);

        PriceQuote cached = estimateCache.get(key);

        if (cached != null) {
            return cached;
        }

        PriceQuote estimated =
                estimateWithAI(
                        destination,
                        attractionName);

        estimateCache.put(key, estimated);

        return estimated;
    }

    private PriceQuote findDocumentedPrice(
            String destination,
            String attractionName) {

        if (destination == null
                || attractionName == null) {
            return unknownQuote();
        }

        PriceRecord record = prices.get(
                makeKey(destination, attractionName));

        if (record == null) {
            return unknownQuote();
        }

        double rate = getUsdRate(record.currency);

        if (!Double.isFinite(rate) || rate <= 0) {
            return unknownQuote();
        }

        double amountUsd =
                Math.round(record.amount * rate * 100.0)
                        / 100.0;

        if (!Double.isFinite(amountUsd)
                || amountUsd < 0) {
            return unknownQuote();
        }

        return new PriceQuote(
                amountUsd,
                PriceStatus.DOCUMENTED,
                record.sourceURL,
                "Catalog price checked on "
                        + record.checkedDate
                        + "; converted from "
                        + record.currency);
    }

    private PriceQuote estimateWithAI(
            String destination,
            String attractionName) {

        try {
            String safeDestination =
                    cleanInput(destination);

            String safeAttraction =
                    cleanInput(attractionName);

            String prompt =
                    "You are helping a travel-planning application "
                    + "estimate attraction admission fees.\n"
                    + "Destination: " + safeDestination + "\n"
                    + "Attraction: " + safeAttraction + "\n\n"

                    + "Return a cautious approximate adult "
                    + "admission price in USD only if you have "
                    + "reasonable knowledge of the attraction.\n"

                    + "Do not invent a current ticket price.\n"
                    + "Do not assume an attraction is free.\n"
                    + "Do not claim to have checked a website.\n"
                    + "Do not invent a source URL.\n"
                    + "If this is an archaeological site, "
                    + "monument, or area without separate "
                    + "admission, be careful to distinguish "
                    + "the site from any paid complex.\n"
                    + "If the attraction is ambiguous, "
                    + "unfamiliar, or the price cannot be "
                    + "reasonably estimated, return UNKNOWN.\n\n"

                    + "Respond in exactly two lines:\n"
                    + "PRICE_USD=number or UNKNOWN\n"
                    + "BASIS=brief explanation\n\n"

                    + "The number is an unverified estimate, "
                    + "not a confirmed or current ticket price.";

            String response = llmClient.generate(prompt);

            if (response == null
                    || response.isBlank()) {
                return unknownQuote();
            }

            Matcher priceMatcher = Pattern.compile(
                    "(?im)^\\s*PRICE_USD\\s*=\\s*"
                    + "(UNKNOWN|[0-9]+(?:\\.[0-9]{1,2})?)\\s*$")
                    .matcher(response);

            if (!priceMatcher.find()) {
                return unknownQuote();
            }

            String priceText =
                    priceMatcher.group(1).trim();

            if ("UNKNOWN".equalsIgnoreCase(priceText)) {
                return unknownQuote();
            }

            double amount =
                    Double.parseDouble(priceText);

            if (!Double.isFinite(amount)
                    || amount < 0
                    || amount > 10000) {
                return unknownQuote();
            }

            Matcher basisMatcher = Pattern.compile(
                    "(?im)^\\s*BASIS\\s*=\\s*(.+)$")
                    .matcher(response);

            if (!basisMatcher.find()) {
                return unknownQuote();
            }

            String explanation =
                    basisMatcher.group(1).trim();

            if (explanation.isBlank()) {
                return unknownQuote();
            }

            if (explanation.length() > 300) {
                explanation =
                        explanation.substring(0, 300);
            }

            return new PriceQuote(
                    Math.round(amount * 100.0) / 100.0,
                    PriceStatus.AI_ESTIMATE,
                    "LLM knowledge; no live price verification",
                    explanation);

        } catch (RuntimeException e) {

            System.out.println(
                    "TripGoAI: AI price estimation unavailable: "
                    + e.getMessage());

            return unknownQuote();
        }
    }

    private PriceQuote unknownQuote() {
        return new PriceQuote(
                Double.NaN,
                PriceStatus.UNKNOWN,
                "",
                "No reliable admission price available.");
    }

    private String cleanInput(String input) {
        return input.replaceAll("[\\r\\n\\t]+", " ")
                .trim()
                .substring(0, Math.min(
                        150,
                        input.replaceAll(
                                "[\\r\\n\\t]+", " ")
                                .trim().length()));
    }

    private void loadPriceCatalog() {

        if (!Files.isRegularFile(PRICE_FILE)) {

            System.out.println(
                    "TripGoAI: Optional attraction price "
                    + "catalog not found at "
                    + PRICE_FILE.toAbsolutePath());

            return;
        }

        try {
            List<String> lines = Files.readAllLines(
                    PRICE_FILE,
                    StandardCharsets.UTF_8);

            LocalDate today = LocalDate.now();

            for (int index = 0;
                    index < lines.size();
                    index++) {

                String line = lines.get(index).trim();

                if (line.isEmpty()
                        || line.startsWith("#")) {
                    continue;
                }

                String[] fields = line.split("\\t", -1);

                if (fields.length != 6) {
                    continue;
                }

                if (fields[0].equalsIgnoreCase(
                        "destination")) {
                    continue;
                }

                try {
                    String destination =
                            fields[0].trim();

                    String attraction =
                            fields[1].trim();

                    double amount =
                            Double.parseDouble(
                                    fields[2].trim());

                    String currency =
                            fields[3].trim()
                                    .toUpperCase(Locale.ROOT);

                    String sourceURL =
                            fields[4].trim();

                    LocalDate checkedDate =
                            LocalDate.parse(
                                    fields[5].trim());

                    if (destination.isEmpty()
                            || attraction.isEmpty()
                            || !Double.isFinite(amount)
                            || amount < 0
                            || !currency.matches("[A-Z]{3}")
                            || !isHttpsURL(sourceURL)
                            || checkedDate.isAfter(today)
                            || checkedDate.isBefore(
                                    today.minusDays(
                                            MAX_AGE_DAYS))) {
                        continue;
                    }

                    prices.put(
                            makeKey(
                                    destination,
                                    attraction),
                            new PriceRecord(
                                    amount,
                                    currency,
                                    sourceURL,
                                    checkedDate));

                } catch (NumberFormatException
                        | DateTimeParseException e) {

                    System.out.println(
                            "TripGoAI: Skipping invalid "
                            + "price record on line "
                            + (index + 1));
                }
            }

            System.out.println(
                    "TripGoAI: Loaded "
                    + prices.size()
                    + " documented attraction prices.");

        } catch (IOException e) {

            System.out.println(
                    "TripGoAI: Price catalog unavailable: "
                    + e.getMessage());
        }
    }

    private boolean isHttpsURL(String url) {

        try {
            URI uri = URI.create(url);

            return "https".equalsIgnoreCase(
                    uri.getScheme())
                    && uri.getHost() != null;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String makeKey(
            String destination,
            String attractionName) {

        return normalize(destination)
                + "|"
                + normalize(attractionName);
    }

    private String normalize(String text) {

        return text.toLowerCase(Locale.ROOT)
                .replaceAll("\\([^)]*\\)", " ")
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double getUsdRate(String currency) {

        if ("USD".equals(currency)) {
            return 1.0;
        }

        if (exchangeRates.containsKey(currency)) {
            return exchangeRates.get(currency);
        }

        String url =
                "https://api.frankfurter.dev/v1/latest"
                + "?base=" + currency
                + "&symbols=USD";

        double result = Double.NaN;

        try {
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(12))
                            .header(
                                    "Accept",
                                    "application/json")
                            .header(
                                    "User-Agent",
                                    "TripGoAI/1.0")
                            .GET()
                            .build();

            HttpResponse<String> response =
                    CLIENT.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString(
                                            StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {

                Matcher matcher = Pattern.compile(
                        "\"USD\"\\s*:\\s*"
                        + "([0-9]+(?:\\.[0-9]+)?)")
                        .matcher(response.body());

                if (matcher.find()) {
                    result = Double.parseDouble(
                            matcher.group(1));
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "TripGoAI: Exchange-rate lookup failed: "
                    + e.getMessage());

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.out.println(
                    "TripGoAI: Exchange-rate lookup interrupted.");

        } catch (RuntimeException e) {

            System.out.println(
                    "TripGoAI: Invalid exchange-rate response.");
        }

        if (!Double.isFinite(result)
                || result <= 0) {
            result = Double.NaN;
        }

        exchangeRates.put(currency, result);

        return result;
    }
}
