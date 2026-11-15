
package com.tripgoai;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherTool implements TravelTool {

    private static final String GEOCODING_URL =
            "https://geocoding-api.open-meteo.com/v1/search";

    private static final String WEATHER_URL =
            "https://api.open-meteo.com/v1/forecast";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Override
    public String getName() {
        return "weather";
    }

    @Override
    public String execute(String destination) {

        if (destination == null || destination.isBlank()) {
            return "Please provide a destination.";
        }

        try {
            Coordinates coordinates = geocode(destination);

            String url = WEATHER_URL
                    + "?latitude=" + coordinates.latitude
                    + "&longitude=" + coordinates.longitude
                    + "&current=temperature_2m,"
                    + "precipitation,weather_code,wind_speed_10m"
                    + "&timezone=auto";

            String json = sendGet(url);

            double temperature =
                    getNumber(json, "temperature_2m");

            double precipitation =
                    getNumber(json, "precipitation");

            double windSpeed =
                    getNumber(json, "wind_speed_10m");

            int weatherCode =
                    (int) getNumber(json, "weather_code");

            StringBuilder output = new StringBuilder();

            output.append("Current Weather in ")
                    .append(destination)
                    .append(":\n\n");

            output.append("Temperature: ")
                    .append(temperature)
                    .append(" °C\n");

            output.append("Precipitation: ")
                    .append(precipitation)
                    .append(" mm\n");

            output.append("Wind Speed: ")
                    .append(windSpeed)
                    .append(" km/h\n");

            output.append("Conditions: ")
                    .append(describeWeather(weatherCode))
                    .append("\n\n");

            output.append("Travel Advice:\n");

            if (isBadWeather(weatherCode)
                    || precipitation > 0) {

                output.append(
                        "- Consider indoor activities.\n"
                        + "- Check outdoor attraction conditions.\n"
                        + "- Carry suitable rain protection.\n");

            } else {
                output.append(
                        "- Conditions may be suitable for sightseeing.\n"
                        + "- Check the forecast again before leaving.\n");
            }

            output.append(
                    "\nSource: Open-Meteo. "
                    + "Conditions can change.");

            return output.toString();

        } catch (IllegalStateException e) {
            return "Unable to retrieve weather: "
                    + e.getMessage();
        }
    }

    public String getForecast(
            String destination, LocalDate date) {

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Destination cannot be empty.");
        }

        if (date == null) {
            throw new IllegalArgumentException(
                    "Forecast date cannot be null.");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Forecast date cannot be in the past.");
        }

        if (date.isAfter(LocalDate.now().plusDays(15))) {
            return "Forecast unavailable for " + date
                    + ". Open-Meteo forecasts are limited "
                    + "to approximately 16 days ahead.";
        }

        Coordinates coordinates = geocode(destination);

        String url = WEATHER_URL
                + "?latitude=" + coordinates.latitude
                + "&longitude=" + coordinates.longitude
                + "&daily=weather_code,"
                + "temperature_2m_max,"
                + "temperature_2m_min,"
                + "precipitation_sum"
                + "&timezone=auto"
                + "&start_date=" + date
                + "&end_date=" + date;

        String json = sendGet(url);

        int code = (int) getFirstArrayNumber(
                json, "weather_code");

        double maxTemp = getFirstArrayNumber(
                json, "temperature_2m_max");

        double minTemp = getFirstArrayNumber(
                json, "temperature_2m_min");

        double rain = getFirstArrayNumber(
                json, "precipitation_sum");

        StringBuilder result = new StringBuilder();

        result.append("Weather Forecast for ")
                .append(destination)
                .append(" on ")
                .append(date)
                .append(":\n");

        result.append("Conditions: ")
                .append(describeWeather(code))
                .append("\n");

        result.append("Temperature: ")
                .append(minTemp)
                .append(" °C to ")
                .append(maxTemp)
                .append(" °C\n");

        result.append("Expected Precipitation: ")
                .append(rain)
                .append(" mm\n");

        if (isBadWeather(code) || rain >= 2.0) {
            result.append(
                    "Recommendation: Consider indoor activities.");
        } else {
            result.append(
                    "Recommendation: Outdoor activities may be suitable.");
        }

        return result.toString();
    }

    public boolean shouldAvoidOutdoorActivities(
            String destination, LocalDate date) {

        if (date == null) {
            throw new IllegalArgumentException(
                    "Date cannot be null.");
        }

        if (date.isBefore(LocalDate.now())
                || date.isAfter(LocalDate.now().plusDays(15))) {
            throw new IllegalArgumentException(
                    "Forecast unavailable for the requested date.");
        }

        Coordinates coordinates = geocode(destination);

        String url = WEATHER_URL
                + "?latitude=" + coordinates.latitude
                + "&longitude=" + coordinates.longitude
                + "&daily=weather_code,precipitation_sum"
                + "&timezone=auto"
                + "&start_date=" + date
                + "&end_date=" + date;

        String json = sendGet(url);

        int code = (int) getFirstArrayNumber(
                json, "weather_code");

        double rain = getFirstArrayNumber(
                json, "precipitation_sum");

        return isBadWeather(code) || rain >= 2.0;
    }

    private Coordinates geocode(String destination) {

        String encoded = URLEncoder.encode(
                destination, StandardCharsets.UTF_8);

        String url = GEOCODING_URL
                + "?name=" + encoded
                + "&count=1&language=en&format=json";

        String json = sendGet(url);

        int resultsIndex = json.indexOf("\"results\"");

        if (resultsIndex < 0
                || json.substring(resultsIndex).matches(
                        "(?s)^.*\"results\"\\s*:\\s*\\[\\s*\\].*$")) {

            throw new IllegalStateException(
                    "Destination could not be located.");
        }

        double latitude =
                getNumber(json, "latitude");

        double longitude =
                getNumber(json, "longitude");

        return new Coordinates(latitude, longitude);
    }

    private String sendGet(String url) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Weather API returned HTTP "
                        + response.statusCode());
            }

            return response.body();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Weather network request failed.", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Weather request interrupted.", e);
        }
    }

    private double getNumber(String json, String key) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalStateException(
                    "Missing weather field: " + key);
        }

        return Double.parseDouble(matcher.group(1));
    }

    private double getFirstArrayNumber(
            String json, String key) {

        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*\\[\\s*(-?\\d+(?:\\.\\d+)?)");

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalStateException(
                    "Missing forecast field: " + key);
        }

        return Double.parseDouble(matcher.group(1));
    }

    private boolean isBadWeather(int code) {

        return code >= 51;
    }

    private String describeWeather(int code) {

        if (code == 0) {
            return "Clear sky";
        }

        if (code >= 1 && code <= 3) {
            return "Partly cloudy or overcast";
        }

        if (code == 45 || code == 48) {
            return "Fog";
        }

        if (code >= 51 && code <= 67) {
            return "Rain or drizzle";
        }

        if (code >= 71 && code <= 77) {
            return "Snow";
        }

        if (code >= 80 && code <= 82) {
            return "Rain showers";
        }

        if (code >= 85 && code <= 86) {
            return "Snow showers";
        }

        if (code >= 95) {
            return "Thunderstorm";
        }

        return "Weather conditions unavailable";
    }

    private static class Coordinates {

        final double latitude;
        final double longitude;

        Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
