
package com.tripgoai;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public final class ItineraryCommandFactory {

    public ItineraryCommand createCommand(
            Itinerary itinerary,
            TripRequest request,
            String aiResponse,
            String originalInstruction) {

        if (itinerary == null || request == null) {
            throw new IllegalArgumentException(
                    "Trip and itinerary details are required.");
        }

        if (originalInstruction == null
                || originalInstruction.isBlank()) {
            throw new IllegalArgumentException(
                    "An editing instruction is required.");
        }

        if (aiResponse == null || aiResponse.isBlank()) {
            throw new IllegalArgumentException(
                    "AI did not provide an editing instruction.");
        }

        String line = aiResponse.strip()
                .lines()
                .filter(s -> !s.isBlank())
                .findFirst()
                .orElse("");

        String[] fields = line.split("\\|", -1);

        String action = fields[0]
                .trim()
                .toUpperCase(Locale.ROOT);

        switch (action) {

            case "CLARIFY": {

                String question = fields.length > 1
                        ? fields[1].trim()
                        : "Please clarify the requested edit.";

                throw new IllegalArgumentException(
                        question.isBlank()
                        ? "Please clarify the requested edit."
                        : question);
            }

            case "REMOVE": {

                requireFields(fields, 2);

                Activity original = findActivity(
                        itinerary, fields[1]);

                return new RemoveActivityCommand(
                        itinerary, original);
            }

            case "MOVE": {

                requireFields(fields, 4);

                Activity original = findActivity(
                        itinerary, fields[1]);

                LocalDate date = readDate(
                        fields[2], request);

                LocalTime time = readTime(fields[3]);

                validateDuration(
                        time,
                        original.getDurationMinutes());

                Activity replacement = copyActivity(
                        original,
                        original.getName(),
                        date,
                        time,
                        original.getEstimatedCost(),
                        original.isOutdoor());

                return new MoveActivityCommand(
                        itinerary, original, replacement);
            }

            case "REPLACE": {

                requireFields(fields, 3);

                Activity original = findActivity(
                        itinerary, fields[1]);

                String newName = readName(
                        fields[2], originalInstruction);

                Activity replacement = copyActivity(
                        original,
                        newName,
                        original.getDate(),
                        original.getStartTime(),
                        Double.NaN,
                        isOutdoorAttraction(newName));

                return new ReplaceActivityCommand(
                        itinerary, original, replacement);
            }

            case "ADD": {

                requireFields(fields, 5);

                String name = readName(
                        fields[1], originalInstruction);

                LocalDate date = readDate(
                        fields[2], request);

                LocalTime time = readTime(fields[3]);

                int minutes = parseInteger(fields[4]);

                if (minutes < 15 || minutes > 720) {
                    throw new IllegalArgumentException(
                            "Activity duration must be "
                            + "between 15 and 720 minutes.");
                }

                validateDuration(time, minutes);

                int nextId = 1;

                for (Activity existing
                        : itinerary.getActivities()) {

                    nextId = Math.max(
                            nextId,
                            existing.getActivityId() + 1);
                }

                Activity added = new Activity(
                        nextId,
                        name,
                        request.getDestination(),
                        date,
                        time,
                        minutes,
                        Double.NaN,
                        "Attraction",
                        isOutdoorAttraction(name));

                return new AddActivityCommand(
                        itinerary, added);
            }

            default:
                throw new IllegalArgumentException(
                        "AI could not interpret the edit. "
                        + "Please describe it more precisely.");
        }
    }

    private static void requireFields(
            String[] fields,
            int expected) {

        if (fields.length != expected) {
            throw new IllegalArgumentException(
                    "Incomplete or ambiguous editing instruction.");
        }
    }

    private static Activity findActivity(
            Itinerary itinerary,
            String indexText) {

        int index = parseInteger(indexText);

        List<Activity> activities =
                itinerary.getActivities();

        if (index < 1 || index > activities.size()) {
            throw new IllegalArgumentException(
                    "The requested activity does not exist.");
        }

        return activities.get(index - 1);
    }

    private static int parseInteger(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "AI selected an invalid activity number. "
                    + "Please specify an activity that exists "
                    + "in your itinerary.", e);
        }
    }

    private static LocalDate readDate(
            String text,
            TripRequest request) {

        try {

            LocalDate date = LocalDate.parse(text.trim());

            if (date.isBefore(request.getStartDate())
                    || date.isAfter(request.getEndDate())) {

                throw new IllegalArgumentException(
                        "The requested date is outside "
                        + "the trip dates.");
            }

            return date;

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Invalid activity date.", e);
        }
    }

    private static LocalTime readTime(String text) {

        try {
            return LocalTime.parse(text.trim());

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Invalid activity time. Use HH:mm.", e);
        }
    }

    private static void validateDuration(
            LocalTime time,
            int minutes) {

        if (time == null || minutes <= 0) {
            throw new IllegalArgumentException(
                    "Invalid activity timing.");
        }

        int endMinute =
                time.toSecondOfDay() / 60 + minutes;

        if (endMinute > 1440) {
            throw new IllegalArgumentException(
                    "Activity cannot extend past midnight.");
        }
    }

    private static String readName(
            String text,
            String originalInstruction) {

        String name = text.trim();

        if (name.isBlank()
                || name.length() > 120
                || name.contains("\n")
                || name.contains("\r")) {

            throw new IllegalArgumentException(
                    "A valid activity name is required.");
        }

        // The agent must not invent a new activity
        // that was not specified by the traveler.
        String requested = originalInstruction
                .toLowerCase(Locale.ROOT);

        if (!requested.contains(
                name.toLowerCase(Locale.ROOT))) {

            throw new IllegalArgumentException(
                    "The new attraction was not explicitly "
                    + "specified by the traveler.");
        }

        return name;
    }

    private static boolean isOutdoorAttraction(
            String name) {

        String lower = name.toLowerCase(Locale.ROOT);

        return lower.contains("park")
                || lower.contains("garden")
                || lower.contains("beach")
                || lower.contains("square")
                || lower.contains("fountain")
                || lower.contains("trail")
                || lower.contains("bridge")
                || lower.contains("viewpoint");
    }

    private static Activity copyActivity(
            Activity original,
            String name,
            LocalDate date,
            LocalTime time,
            double cost,
            boolean outdoor) {

        return new Activity(
                original.getActivityId(),
                name,
                original.getLocation(),
                date,
                time,
                original.getDurationMinutes(),
                cost,
                original.getCategory(),
                outdoor);
    }

    // Command Pattern: Add Activity
    private static final class AddActivityCommand
            implements ItineraryCommand {

        private final Itinerary itinerary;
        private final Activity activity;
        private boolean executed;

        AddActivityCommand(
                Itinerary itinerary,
                Activity activity) {

            this.itinerary = itinerary;
            this.activity = activity;
        }

        @Override
        public void execute() {

            if (executed) {
                throw new IllegalStateException(
                        "Command already executed.");
            }

            itinerary.addActivity(activity);
            executed = true;
        }

        @Override
        public void undo() {

            if (!executed) {
                return;
            }

            itinerary.removeActivity(activity);
            executed = false;
        }
    }

    // Command Pattern: Remove Activity
    private static final class RemoveActivityCommand
            implements ItineraryCommand {

        private final Itinerary itinerary;
        private final Activity activity;
        private boolean executed;

        RemoveActivityCommand(
                Itinerary itinerary,
                Activity activity) {

            this.itinerary = itinerary;
            this.activity = activity;
        }

        @Override
        public void execute() {

            if (executed) {
                throw new IllegalStateException(
                        "Command already executed.");
            }

            itinerary.removeActivity(activity);
            executed = true;
        }

        @Override
        public void undo() {

            if (!executed) {
                return;
            }

            itinerary.addActivity(activity);
            executed = false;
        }
    }

    // Shared Command behavior for moving/replacing.
    private static class ChangeActivityCommand
            implements ItineraryCommand {

        private final Itinerary itinerary;
        private final Activity original;
        private final Activity replacement;
        private boolean executed;

        ChangeActivityCommand(
                Itinerary itinerary,
                Activity original,
                Activity replacement) {

            this.itinerary = itinerary;
            this.original = original;
            this.replacement = replacement;
        }

        @Override
        public void execute() {

            if (executed) {
                throw new IllegalStateException(
                        "Command already executed.");
            }

            itinerary.removeActivity(original);

            try {

                itinerary.addActivity(replacement);
                executed = true;

            } catch (RuntimeException e) {

                itinerary.addActivity(original);
                throw e;
            }
        }

        @Override
        public void undo() {

            if (!executed) {
                return;
            }

            itinerary.removeActivity(replacement);
            itinerary.addActivity(original);
            executed = false;
        }
    }

    // Command Pattern: Move Activity
    private static final class MoveActivityCommand
            extends ChangeActivityCommand {

        MoveActivityCommand(
                Itinerary itinerary,
                Activity original,
                Activity replacement) {

            super(itinerary, original, replacement);
        }
    }

    // Command Pattern: Replace Activity
    private static final class ReplaceActivityCommand
            extends ChangeActivityCommand {

        ReplaceActivityCommand(
                Itinerary itinerary,
                Activity original,
                Activity replacement) {

            super(itinerary, original, replacement);
        }
    }
}
