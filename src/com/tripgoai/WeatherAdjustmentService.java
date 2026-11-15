
package com.tripgoai;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WeatherAdjustmentService {

    private final TravelAgent agent;
    private final WeatherTool weatherTool;
    private final AttractionSearchTool attractionTool;

    public static final class Result {

        private final String report;
        private final boolean modified;

        private Result(String report, boolean modified) {
            this.report = report;
            this.modified = modified;
        }

        public String getReport() {
            return report;
        }

        public boolean isModified() {
            return modified;
        }
    }

    public WeatherAdjustmentService(TravelAgent agent) {
        this(agent, new WeatherTool(),
                new AttractionSearchTool());
    }

    // Also allows controlled tests with replacement tools.
    WeatherAdjustmentService(
            TravelAgent agent,
            WeatherTool weatherTool,
            AttractionSearchTool attractionTool) {

        if (agent == null || weatherTool == null
                || attractionTool == null) {
            throw new IllegalArgumentException(
                    "Weather adjustment dependencies are required.");
        }

        this.agent = agent;
        this.weatherTool = weatherTool;
        this.attractionTool = attractionTool;
    }

    public Result adjust(
            String destination,
            Itinerary itinerary,
            TripRequest request) {

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Destination is required.");
        }

        if (itinerary == null) {
            throw new IllegalArgumentException(
                    "An existing itinerary is required.");
        }

        if (request != null
                && !destination.equalsIgnoreCase(
                        request.getDestination())) {
            throw new IllegalArgumentException(
                    "Trip destination does not match the request.");
        }

        List<Activity> original =
                itinerary.getSortedActivities();

        if (original.isEmpty()) {
            return unchanged(
                    "The itinerary has no activities.");
        }

        Set<LocalDate> dates = new LinkedHashSet<>();

        for (Activity activity : original) {

            if (activity.getDate() == null
                    || activity.getStartTime() == null
                    || activity.getDurationMinutes() <= 0) {
                return unchanged(
                        "An activity has incomplete scheduling "
                        + "information.");
            }

            if (request != null
                    && (activity.getDate().isBefore(
                            request.getStartDate())
                        || activity.getDate().isAfter(
                            request.getEndDate()))) {
                return unchanged(
                        "An activity is outside the trip dates.");
            }

            dates.add(activity.getDate());
        }

        // Verify all trip dates before doing any work
        // that could change the itinerary.
        LocalDate today = LocalDate.now();
        LocalDate lastForecastDate =
                today.plusDays(15);

        for (LocalDate date : dates) {

            if (date.isBefore(today)
                    || date.isAfter(lastForecastDate)) {
                return unchanged(
                        "Forecast unavailable for " + date
                        + ". Weather adjustments require "
                        + "dates within the supported "
                        + "forecast window.");
            }
        }

        Map<LocalDate, Boolean> weatherWarnings =
                new LinkedHashMap<>();

        // Retrieve all forecasts before selecting changes.
        // If one fails, preserve the entire itinerary.
        try {
            for (LocalDate date : dates) {

                boolean unsafe =
                        weatherTool.shouldAvoidOutdoorActivities(
                                destination, date);

                weatherWarnings.put(date, unsafe);
            }

        } catch (RuntimeException e) {

            return unchanged(
                    "Weather information could not be "
                    + "retrieved: " + e.getMessage());
        }

        List<Activity> affected = new ArrayList<>();

        for (Activity activity : original) {

            boolean badWeather = weatherWarnings.get(
                    activity.getDate());

            if (badWeather && activity.isOutdoor()) {

                if (!isReplaceableAttraction(activity)) {
                    return unchanged(
                            "An outdoor activity requires "
                            + "a compatible replacement type: "
                            + activity.getName()
                            + ". Automatic attraction replacement "
                            + "was not applied.");
                }

                affected.add(activity);
            }
        }

        if (affected.isEmpty()) {

            return unchanged(
                    "Forecasts were checked for all "
                    + "scheduled dates.\n"
                    + "No flagged outdoor attractions "
                    + "require adjustment.");
        }

        List<Activity> candidates;

        try {
            candidates =
                    attractionTool.getActivityCandidates(
                            destination);

        } catch (RuntimeException e) {

            return unchanged(
                    "Unable to retrieve replacement "
                    + "attractions: " + e.getMessage());
        }

        Set<String> existingNames = new HashSet<>();

        for (Activity activity : original) {
            existingNames.add(
                    normalize(activity.getName()));
        }

        Map<String, Activity> indoorOptions =
                new LinkedHashMap<>();

        for (Activity candidate : candidates) {

            if (candidate == null
                    || candidate.getName() == null
                    || candidate.getName().isBlank()) {
                continue;
            }

            if (!isPotentiallyIndoor(candidate)) {
                continue;
            }

            String name = normalize(candidate.getName());

            if (!existingNames.contains(name)) {
                indoorOptions.putIfAbsent(name, candidate);
            }
        }

        List<Activity> available =
                new ArrayList<>(indoorOptions.values());

        if (available.size() < affected.size()) {
            return unchanged(
                    "Not enough different potentially "
                    + "indoor attractions were retrieved "
                    + "for the affected activities.");
        }

        String prompt = buildPrompt(
                destination, affected, available, request);

        String response;

        try {
            response = agent.askAI(prompt);

        } catch (RuntimeException e) {

            return unchanged(
                    "AI replacement selection was "
                    + "unavailable: " + e.getMessage());
        }

        List<Activity> ranked =
                rankCandidates(response, available);

        // AI must make at least one valid selection.
        if (ranked.isEmpty()) {
            return unchanged(
                    "AI did not select any valid "
                    + "replacement attractions.");
        }

        // Complete an incomplete AI ranking using only
        // retrieved, potentially indoor attractions.
        Set<String> rankedNames = new HashSet<>();

        for (Activity activity : ranked) {
            rankedNames.add(normalize(activity.getName()));
        }

        for (Activity activity : available) {

            if (rankedNames.add(
                    normalize(activity.getName()))) {
                ranked.add(activity);
            }
        }

        Map<Activity, Activity> replacements =
                new IdentityHashMap<>();

        double knownCost = itinerary.getTotalCost();

        for (Activity oldActivity : affected) {

            if (oldActivity.hasKnownCost()) {
                knownCost -= oldActivity.getEstimatedCost();
            }
        }

        Double budget = request == null
                ? null : request.getMaxBudget();

        Set<String> selectedNames = new HashSet<>();

        for (Activity oldActivity : affected) {

            Activity selected = null;

            for (Activity candidate : ranked) {

                String name = normalize(
                        candidate.getName());

                if (selectedNames.contains(name)) {
                    continue;
                }

                if (budget != null
                        && candidate.hasKnownCost()
                        && knownCost
                            + candidate.getEstimatedCost()
                            > budget + 0.000001) {
                    continue;
                }

                selected = candidate;
                selectedNames.add(name);
                break;
            }

            if (selected == null) {

                return unchanged(
                        "No suitable replacement could "
                        + "be selected within the "
                        + "available known-cost budget.");
            }

            Activity replacement = new Activity(
                    oldActivity.getActivityId(),
                    selected.getName(),
                    selected.getLocation(),
                    oldActivity.getDate(),
                    oldActivity.getStartTime(),
                    oldActivity.getDurationMinutes(),
                    selected.getEstimatedCost(),
                    selected.getCategory(),
                    false);

            replacements.put(oldActivity, replacement);

            if (selected.hasKnownCost()) {
                knownCost += selected.getEstimatedCost();
            }
        }

        // Build and validate a separate draft first.
        // Nothing in the original itinerary has changed.
        Itinerary draft = new Itinerary(
                itinerary.getItineraryId());

        for (Activity activity : original) {

            Activity replacement =
                    replacements.get(activity);

            draft.addActivity(
                    replacement == null
                    ? activity : replacement);
        }

        ScheduleManager schedule = new ScheduleManager();

        try {
            if (schedule.checkConflicts(draft)) {
                throw new IllegalStateException(
                        "Schedule overlaps detected.");
            }

            if (schedule.hasIncompleteSchedule(draft)) {
                throw new IllegalStateException(
                        "Incomplete schedule.");
            }

            if (request != null) {
                agent.validateItinerary(request, draft);
            }

        } catch (RuntimeException e) {

            return unchanged(
                    "Proposed adjustment failed validation: "
                    + e.getMessage());
        }

        // Commit only after the entire draft passes.
        for (Activity activity : original) {

            if (replacements.containsKey(activity)) {
                itinerary.removeActivity(activity);
            }
        }

        for (Activity activity : original) {

            Activity replacement =
                    replacements.get(activity);

            if (replacement != null) {
                itinerary.addActivity(replacement);
            }
        }

        StringBuilder report = new StringBuilder();

        report.append(
                "=== WEATHER-AWARE ITINERARY ADJUSTMENT ===\n");

        report.append("\nFORECAST REVIEW\n");

        for (Map.Entry<LocalDate, Boolean> entry
                : weatherWarnings.entrySet()) {

            report.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue()
                            ? "Outdoor activity warning"
                            : "No outdoor activity warning")
                    .append("\n");
        }

        report.append("\nACTIVITIES REPLACED\n");

        for (Activity oldActivity : affected) {

            Activity replacement =
                    replacements.get(oldActivity);

            report.append(oldActivity.getDate())
                    .append(" ")
                    .append(oldActivity.getStartTime())
                    .append("\n");

            report.append("Before: ")
                    .append(oldActivity.getName())
                    .append("\n");

            report.append("After:  ")
                    .append(replacement.getName())
                    .append("\n\n");
        }

        report.append(
                "=== UPDATED ITINERARY ===\n\n");

        report.append(itinerary);

        BudgetManager manager = new BudgetManager();

        manager.calculateTotal(itinerary);

        report.append("\n\n=== BUDGET REVIEW ===\n");

        if (budget != null) {

            report.append(
                    manager.getBudgetReport(budget));

        } else {

            report.append(
                    "No original budget limit is "
                    + "available for this itinerary.\n");

            report.append(String.format(
                    Locale.US,
                    "Known estimated cost: $%.2f%n",
                    itinerary.getTotalCost()));
        }

        if (itinerary.hasUnknownCosts()) {

            report.append(
                    "\nWARNING: Some activity prices "
                    + "are unknown. Full affordability "
                    + "cannot be confirmed.");
        }

        report.append(
                "\n\n=== SCHEDULE REVIEW ===\n");

        report.append(
                "No overlapping activities detected.\n");

        report.append(
                "Visit durations were retained from "
                + "the original itinerary.\n"
                + "Travel times, indoor access, opening "
                + "hours and availability are unverified.\n");

        report.append(
                "\nChanges applied to the current itinerary. "
                + "Save the trip to persist them.");

        return new Result(report.toString(), true);
    }

    private String buildPrompt(
            String destination,
            List<Activity> affected,
            List<Activity> candidates,
            TripRequest request) {

        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "You are TripGoAI's weather-adjustment agent.\n"
                + "Choose suitable indoor alternatives "
                + "for outdoor attractions affected "
                + "by forecast weather.\n"
                + "Only select from the listed candidates.\n");

        prompt.append("Destination: ")
                .append(destination)
                .append("\n");

        if (request != null) {

            prompt.append("Traveler interests: ")
                    .append(request.getInterests())
                    .append("\n");

            prompt.append("Maximum activity budget: ")
                    .append(request.getMaxBudget())
                    .append("\n");
        }

        prompt.append("Stored preferences: ")
                .append(
                    agent.getMemoryManager()
                            .getPreferences())
                .append("\n\n");

        prompt.append("Affected outdoor activities:\n");

        for (Activity activity : affected) {

            prompt.append(activity.getDate())
                    .append(" | ")
                    .append(activity.getName())
                    .append("\n");
        }

        prompt.append(
                "\nPotentially indoor alternatives:\n");

        for (int i = 0; i < candidates.size(); i++) {

            Activity activity = candidates.get(i);

            prompt.append("ID|")
                    .append(i + 1)
                    .append(" | ")
                    .append(activity.getName())
                    .append(" | Cost: ");

            if (activity.hasKnownCost()) {
                prompt.append(
                        activity.getEstimatedCost());
            } else {
                prompt.append("UNKNOWN");
            }

            prompt.append("\n");
        }

        prompt.append(
                "\nReturn ")
                .append(affected.size())
                .append(" unique candidate IDs "
                        + "in preferred order.\n"
                        + "Use the format ID|number.\n"
                        + "Do not invent attractions, prices, "
                        + "opening hours or availability.\n"
                        + "Do not include explanations.");

        return prompt.toString();
    }

    private List<Activity> rankCandidates(
            String response,
            List<Activity> candidates) {

        List<Activity> ranked = new ArrayList<>();

        if (response == null || response.isBlank()) {
            return ranked;
        }

        Set<Integer> used = new HashSet<>();

        Pattern pattern = Pattern.compile(
                "(?i)\\bID\\s*[|:=#]\\s*(\\d+)\\b");

        for (String line : response.split("\\R")) {

            Integer id = null;
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {

                try {
                    id = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }

            } else if (line.trim().matches("\\d+")) {

                try {
                    id = Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            if (id != null
                    && id >= 1
                    && id <= candidates.size()
                    && used.add(id)) {

                ranked.add(candidates.get(id - 1));
            }
        }

        return ranked;
    }

    private boolean isPotentiallyIndoor(
            Activity activity) {

        if (activity.isOutdoor()) {
            return false;
        }

        String text = normalize(
                activity.getName() + " "
                + activity.getCategory());

        return containsAny(text,
                "museum",
                "gallery",
                "aquarium",
                "planetarium",
                "theatre",
                "theater",
                "cinema",
                "library",
                "exhibition",
                "art center",
                "art centre",
                "science center",
                "opera house",
                "indoor");
    }

    private boolean isReplaceableAttraction(
            Activity activity) {

        String category = normalize(
                activity.getCategory());

        return !containsAny(category,
                "restaurant",
                "meal",
                "food",
                "transport",
                "accommodation",
                "hotel");
    }

    private boolean containsAny(
            String text, String... keywords) {

        for (String keyword : keywords) {

            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String text) {

        return text.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private Result unchanged(String reason) {

        return new Result(
                "=== WEATHER-AWARE ITINERARY ADJUSTMENT ===\n"
                + reason
                + "\nNo itinerary changes were made.",
                false);
    }
}
