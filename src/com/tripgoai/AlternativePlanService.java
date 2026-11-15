
package com.tripgoai;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AlternativePlanService {

    private final TravelAgent travelAgent;

    public AlternativePlanService(TravelAgent travelAgent) {
        if (travelAgent == null) {
            throw new IllegalArgumentException(
                    "TravelAgent is required.");
        }
        this.travelAgent = travelAgent;
    }

    public String generateAlternativeReport(
            String destination,
            Itinerary original,
            String reason) {

        return generateAlternativeReport(
                destination, original, reason, null);
    }

    public String generateAlternativeReport(
            String destination,
            Itinerary original,
            String reason,
            Double budgetLimit) {

        Trip originalTrip = new Trip(
                0, destination, original);

        Trip alternative = generateAlternative(
                originalTrip, reason, budgetLimit);

        return formatReport(alternative, budgetLimit);
    }

    public Trip generateAlternative(
            Trip original,
            String reason,
            Double budgetLimit) {

        if (original == null) {
            throw new IllegalArgumentException(
                    "An existing trip is required.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Please describe the alternative you want.");
        }

        if (budgetLimit != null
                && (!Double.isFinite(budgetLimit)
                || budgetLimit <= 0)) {
            throw new IllegalArgumentException(
                    "Invalid budget limit.");
        }

        Itinerary originalItinerary =
                original.getItinerary();

        List<Activity> originalActivities =
                originalItinerary.getSortedActivities();

        if (originalActivities.isEmpty()) {
            throw new IllegalStateException(
                    "The original itinerary is empty.");
        }

        ScheduleManager scheduleManager =
                new ScheduleManager();

        if (scheduleManager.hasIncompleteSchedule(
                originalItinerary)) {
            throw new IllegalStateException(
                    "The original itinerary has "
                    + "incomplete schedule information.");
        }

        if (scheduleManager.checkConflicts(
                originalItinerary)) {
            throw new IllegalStateException(
                    "Resolve existing schedule conflicts first.");
        }

        // Detect a specifically named activity.
        // A general request is allowed to have no target.
        Activity target = findRequestedTarget(
                originalActivities, reason);

        boolean indoorRequested =
                requiresIndoorAlternative(reason);

        Set<String> originalNames = new HashSet<>();

        int replaceableCount = 0;
        double fixedKnownCost = 0;

        for (Activity activity : originalActivities) {
            originalNames.add(
                    normalize(activity.getName()));

            if (target != null) {
                // Targeted request: change only this activity.
                if (activity == target) {
                    replaceableCount++;
                } else if (activity.hasKnownCost()) {
                    fixedKnownCost +=
                            activity.getEstimatedCost();
                }

            } else if (isFixedActivity(activity)) {
                // General request: preserve fixed activities.
                if (activity.hasKnownCost()) {
                    fixedKnownCost +=
                            activity.getEstimatedCost();
                }

            } else {
                replaceableCount++;
            }
        }

        if (replaceableCount == 0) {
            throw new IllegalStateException(
                    "The itinerary contains no "
                    + "replaceable attraction activities.");
        }

        // Retrieve attraction candidates.
        List<Activity> retrieved;

        try {
            AttractionSearchTool attractionTool =
                    new AttractionSearchTool();

            retrieved =
                    attractionTool.getActivityCandidates(
                            original.getDestination());

        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Unable to retrieve alternative "
                    + "attractions: " + e.getMessage(), e);
        }

        Map<String, Activity> unique =
                new LinkedHashMap<>();

        for (Activity candidate : retrieved) {

            if (candidate == null
                    || candidate.getName() == null
                    || candidate.getName().isBlank()) {
                continue;
            }

            // For rain or indoor requests, require a
            // clearly indoor candidate.
            if (indoorRequested
                    && !isClearlyIndoor(candidate)) {
                continue;
            }

            String key = normalize(candidate.getName());

            // Never duplicate an original attraction.
            if (!originalNames.contains(key)) {
                unique.putIfAbsent(key, candidate);
            }
        }

        List<Activity> candidates =
                new ArrayList<>(unique.values());

        if (candidates.isEmpty()) {
            throw new IllegalStateException(
                    "No suitable different attractions were "
                    + "retrieved. The original trip "
                    + "remains unchanged.");
        }

        // FIX: A general request does not need enough
        // candidates to replace every activity.
        int selectionCount = target == null
                ? Math.min(replaceableCount, candidates.size())
                : 1;

        // Ask the existing AI to rank retrieved attractions.
        String prompt = buildPrompt(
                original,
                reason,
                candidates,
                selectionCount,
                budgetLimit);

        String aiResponse = travelAgent.askAI(prompt);

        if (aiResponse == null || aiResponse.isBlank()) {
            throw new IllegalStateException(
                    "AI alternative selection was unavailable.");
        }

        List<Activity> ranked =
                rankCandidates(candidates, aiResponse);

        // Independent draft: original trip is unchanged.
        Itinerary alternativeItinerary =
                new Itinerary(0);

        Set<String> selectedNames = new HashSet<>();

        double knownCost = target == null
                ? originalItinerary.getTotalCost()
                : fixedKnownCost;

        int nextCandidate = 0;
        int changes = 0;

        if (budgetLimit != null
                && knownCost > budgetLimit + 0.000001) {
            throw new IllegalStateException(
                    "Existing activities already "
                    + "exceed the requested budget.");
        }

        for (Activity slot : originalActivities) {

            boolean preserveOriginal =
                    (target != null && slot != target)
                    || (target == null
                    && isFixedActivity(slot));

            if (preserveOriginal) {
                alternativeItinerary.addActivity(slot);
                continue;
            }

            Activity chosen = null;

            // For general requests, the original cost is
            // included in the running total. Remove it
            // before considering a replacement.
            double originalSlotCost =
                    target == null && slot.hasKnownCost()
                    ? slot.getEstimatedCost()
                    : 0;

            double costWithoutSlot =
                    knownCost - originalSlotCost;

            while (nextCandidate < ranked.size()) {

                Activity candidate =
                        ranked.get(nextCandidate++);

                String candidateName =
                        normalize(candidate.getName());

                if (selectedNames.contains(candidateName)) {
                    continue;
                }

                // Check known costs without double-counting
                // the activity being replaced.
                double proposedKnownCost =
                        costWithoutSlot
                        + (candidate.hasKnownCost()
                        ? candidate.getEstimatedCost()
                        : 0);

                if (budgetLimit != null
                        && proposedKnownCost
                        > budgetLimit + 0.000001) {
                    continue;
                }

                chosen = candidate;
                selectedNames.add(candidateName);
                break;
            }

            if (chosen == null) {

                if (target != null) {
                    throw new IllegalStateException(
                            "No suitable replacement fits the "
                            + "requested activity and budget.");
                }

                // FIX: Preserve this activity instead
                // of rejecting the whole general plan.
                alternativeItinerary.addActivity(slot);
                continue;
            }

            // Keep the exact date, start time and duration.
            alternativeItinerary.addActivity(
                    copyForSlot(slot, chosen));

            knownCost = costWithoutSlot
                    + (chosen.hasKnownCost()
                    ? chosen.getEstimatedCost()
                    : 0);

            changes++;
        }

        if (changes == 0) {
            throw new IllegalStateException(
                    "No suitable different attraction fits "
                    + "the budget. The original trip "
                    + "remains unchanged.");
        }

        // Validate the new schedule.
        if (scheduleManager.checkConflicts(
                alternativeItinerary)) {
            throw new IllegalStateException(
                    "Alternative itinerary contains "
                    + "overlapping activities.");
        }

        if (scheduleManager.hasIncompleteSchedule(
                alternativeItinerary)) {
            throw new IllegalStateException(
                    "Alternative schedule is incomplete.");
        }

        // Check known costs.
        if (budgetLimit != null
                && alternativeItinerary.getTotalCost()
                > budgetLimit + 0.000001) {
            throw new IllegalStateException(
                    "Known alternative costs exceed the budget.");
        }

        // Return a separate, unsaved draft.
        return new Trip(
                0,
                original.getDestination(),
                alternativeItinerary);
    }

    // Identify a specifically named activity in the request.
    private Activity findRequestedTarget(
            List<Activity> activities,
            String reason) {

        String lower =
                reason.toLowerCase(Locale.ROOT);

        Activity target = null;

        for (Activity activity : activities) {

            String name = activity.getName();

            if (name == null || name.isBlank()) {
                continue;
            }

            if (lower.contains(
                    name.toLowerCase(Locale.ROOT))) {

                if (target != null && target != activity) {
                    throw new IllegalArgumentException(
                            "Multiple activity names were mentioned. "
                            + "Specify only one to replace.");
                }

                target = activity;
            }
        }

        // FIX: Do not interpret "replace attractions"
        // or "different itinerary" as a named target.
        boolean generalRequest = Pattern.compile(
                "(?i)\\b(replace|change|swap)\\s+"
                + "(?:some\\s+|any\\s+|the\\s+)?"
                + "(?:attractions|activities|stops|places)\\b"
                + "|\\b(different|new|general|another|alternative)"
                + "\\s+itinerary\\b"
                + "|\\bwhere\\s+suitable\\b")
                .matcher(reason).find();

        boolean specificReplacement = Pattern.compile(
                "(?i)\\b(replace|alternative\\s+to|"
                + "instead\\s+of|swap\\s+out)\\b")
                .matcher(reason).find();

        if (target == null
                && specificReplacement
                && !generalRequest) {

            throw new IllegalArgumentException(
                    "The requested activity was not found. "
                    + "No changes were made.");
        }

        if (target == null) {
            return null;
        }

        // Validate ISO date, if supplied.
        Matcher isoDate = Pattern.compile(
                "(?i)\\bon\\s+(\\d{4}-\\d{2}-\\d{2})\\b")
                .matcher(reason);

        if (isoDate.find()) {

            LocalDate requestedDate =
                    LocalDate.parse(isoDate.group(1));

            if (!requestedDate.equals(target.getDate())) {
                throw new IllegalArgumentException(
                        "The named attraction is not scheduled "
                        + "on that date.");
            }
        }

        // Validate a written date, e.g. December 3.
        Matcher namedDate = Pattern.compile(
                "(?i)\\bon\\s+"
                + "(january|february|march|april|may|june|"
                + "july|august|september|october|"
                + "november|december)"
                + "\\s+(\\d{1,2})"
                + "(?:,?\\s+(\\d{4}))?\\b")
                .matcher(reason);

        if (namedDate.find()) {

            Month month = Month.valueOf(
                    namedDate.group(1)
                            .toUpperCase(Locale.ROOT));

            int day = Integer.parseInt(
                    namedDate.group(2));

            int year = namedDate.group(3) == null
                    ? target.getDate().getYear()
                    : Integer.parseInt(
                            namedDate.group(3));

            LocalDate requestedDate =
                    LocalDate.of(year, month, day);

            if (!requestedDate.equals(target.getDate())) {
                throw new IllegalArgumentException(
                        "The named attraction is not scheduled "
                        + "on that date.");
            }
        }

        // Validate start time, if supplied.
        Matcher time = Pattern.compile(
                "(?i)\\bat\\s+(\\d{1,2}:\\d{2})\\b")
                .matcher(reason);

        if (time.find()) {

            LocalTime requestedTime =
                    LocalTime.parse(time.group(1));

            if (!requestedTime.equals(
                    target.getStartTime())) {

                throw new IllegalArgumentException(
                        "The named attraction is not scheduled "
                        + "at that time.");
            }
        }

        return target;
    }

    // Detect requests for indoor alternatives.
    private boolean requiresIndoorAlternative(String reason) {

        return Pattern.compile(
                "(?i)\\b(indoor|indoors|rain|rainy|"
                + "storm|shelter)\\b")
                .matcher(reason)
                .find();
    }

    // Unknown attractions are not automatically
    // considered indoor.
    private boolean isClearlyIndoor(Activity activity) {

        if (activity.isOutdoor()) {
            return false;
        }

        String description =
                (activity.getName() + " "
                + activity.getCategory())
                .toLowerCase(Locale.ROOT);

        return Pattern.compile(
                "(?i)\\b(museum|gallery|exhibition|library|"
                + "aquarium|theatre|theater)\\b")
                .matcher(description)
                .find();
    }

    // Build the AI attraction ranking prompt.
    private String buildPrompt(
            Trip original,
            String reason,
            List<Activity> candidates,
            int requiredCount,
            Double budgetLimit) {

        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "You are TripGoAI's alternative "
                + "travel planning agent.\n");

        prompt.append(
                "Choose suitable different attractions "
                + "from the retrieved list.\n");

        prompt.append("Destination: ")
                .append(original.getDestination())
                .append("\n");

        prompt.append("Traveler request: ")
                .append(reason)
                .append("\n");

        prompt.append("Original itinerary:\n")
                .append(original.getItinerary())
                .append("\n\n");

        prompt.append("Saved preferences:\n")
                .append(
                        travelAgent.getMemoryManager()
                                .getPreferences())
                .append("\n");

        if (budgetLimit != null) {

            prompt.append("Maximum activity budget: $")
                    .append(budgetLimit)
                    .append("\n");

        } else {

            prompt.append(
                    "Budget limit unavailable. "
                    + "Do not assume unlimited money.\n");
        }

        prompt.append(
                "\nRetrieved attraction candidates:\n");

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

        prompt.append("\nSelect ")
                .append(requiredCount)
                .append(" different attractions.\n");

        prompt.append(
                "Prioritize the traveler request, "
                + "preferences and tourist relevance.\n"
                + "Do not invent attractions, prices, "
                + "opening hours or availability.\n"
                + "Return one unique ID per line.\n"
                + "Example:\nID|3\nID|7\nID|2\n"
                + "No explanations or Markdown.");

        return prompt.toString();
    }

    // Convert AI-selected IDs into ranked activities.
    private List<Activity> rankCandidates(
            List<Activity> candidates,
            String aiResponse) {

        List<Activity> ranked = new ArrayList<>();
        Set<Integer> usedIds = new HashSet<>();

        Pattern explicitId = Pattern.compile(
                "(?i)\\bID\\s*[|:=#]\\s*(\\d+)");

        for (String rawLine :
                aiResponse.split("\\R")) {

            String line = rawLine.trim();
            Integer id = null;

            Matcher matcher =
                    explicitId.matcher(line);

            if (matcher.find()) {

                try {
                    id = Integer.parseInt(
                            matcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }

            } else if (line.matches("\\d+")) {

                try {
                    id = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            if (id == null
                    || id < 1
                    || id > candidates.size()
                    || !usedIds.add(id)) {
                continue;
            }

            ranked.add(candidates.get(id - 1));
        }

        // Fallback uses only retrieved candidates.
        for (int i = 0; i < candidates.size(); i++) {

            if (usedIds.add(i + 1)) {
                ranked.add(candidates.get(i));
            }
        }

        return ranked;
    }

    // Copy a selected attraction to the original slot.
    private Activity copyForSlot(
            Activity slot,
            Activity selected) {

        return new Activity(
                slot.getActivityId(),
                selected.getName(),
                selected.getLocation(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getDurationMinutes(),
                selected.getEstimatedCost(),
                selected.getCategory(),
                selected.isOutdoor());
    }

    // Preserve fixed itinerary items.
    private boolean isFixedActivity(Activity activity) {

        String category = activity.getCategory()
                .toLowerCase(Locale.ROOT);

        return category.contains("restaurant")
                || category.contains("meal")
                || category.contains("food")
                || category.contains("transport")
                || category.contains("accommodation")
                || category.contains("hotel");
    }

    private String normalize(String name) {

        return name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    // Preserve the existing report format.
    public String formatReport(
            Trip alternative,
            Double budgetLimit) {

        Itinerary itinerary =
                alternative.getItinerary();

        StringBuilder report = new StringBuilder();

        report.append(
                "=== ALTERNATIVE TRIP PLAN ===\n\n");

        report.append(
                "This is a separate alternative draft. "
                + "Your original trip is unchanged.\n\n");

        report.append(itinerary);

        BudgetManager manager = new BudgetManager();
        manager.calculateTotal(itinerary);

        report.append(
                "\n\n=== BUDGET REPORT ===\n");

        if (budgetLimit != null) {

            report.append(
                    manager.getBudgetReport(budgetLimit));

        } else {

            report.append(
                    "Budget limit unavailable for "
                    + "this trip context.\n");

            report.append(
                    "Known estimated costs: $");

            report.append(String.format(
                    Locale.US,
                    "%.2f",
                    itinerary.getTotalCost()));
        }

        if (itinerary.hasUnknownCosts()) {

            report.append(
                    "\nSome prices are unknown. "
                    + "Full affordability is unconfirmed.");
        }

        report.append(
                "\n\n=== SCHEDULE REPORT ===\n");

        report.append(
                "No overlapping activities detected.\n");

        report.append(
                "Travel times, opening hours, "
                + "availability and visit durations "
                + "have not been fully verified.\n");

        return report.toString();
    }
}
