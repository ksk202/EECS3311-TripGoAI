
package com.tripgoai;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ScheduleManager {

    public enum OpeningStatus {
        FITS_HOURS,
        CONFLICT,
        UNKNOWN
    }

    // Represents one opening interval.
    private static final class TimeWindow {

        final LocalTime opens;
        final LocalTime closes;

        TimeWindow(LocalTime opens, LocalTime closes) {
            this.opens = opens;
            this.closes = closes;
        }

        boolean contains(
                LocalTime start,
                LocalTime end) {

            return !start.isBefore(opens)
                    && !end.isAfter(closes);
        }
    }

    // Opening-hours information supplied by
    // an external source.
    public static final class OpeningHours {

        private final String source;

        private final Map<DayOfWeek, List<TimeWindow>>
                weeklyHours =
                new EnumMap<>(DayOfWeek.class);

        private final Set<DayOfWeek> closedDays =
                EnumSet.noneOf(DayOfWeek.class);

        public OpeningHours(String source) {

            if (source == null || source.isBlank()) {
                throw new IllegalArgumentException(
                        "Opening-hours source is required.");
            }

            this.source = source.trim();
        }

        public String getSource() {
            return source;
        }

        // Add a daytime opening interval.
        // Multiple intervals per day are supported.
        public void addOpenHours(
                DayOfWeek day,
                LocalTime opening,
                LocalTime closing) {

            if (day == null
                    || opening == null
                    || closing == null
                    || !closing.isAfter(opening)) {

                throw new IllegalArgumentException(
                        "Invalid opening-hours interval.");
            }

            closedDays.remove(day);

            weeklyHours
                    .computeIfAbsent(
                            day,
                            key -> new ArrayList<>())
                    .add(new TimeWindow(
                            opening,
                            closing));
        }

        public void markClosed(DayOfWeek day) {

            if (day == null) {
                throw new IllegalArgumentException(
                        "Day cannot be null.");
            }

            weeklyHours.remove(day);
            closedDays.add(day);
        }

        private OpeningStatus assess(
                Activity activity) {

            if (activity.getDate() == null
                    || activity.getStartTime() == null
                    || activity.getDurationMinutes() <= 0) {

                return OpeningStatus.UNKNOWN;
            }

            DayOfWeek day =
                    activity.getDate().getDayOfWeek();

            if (closedDays.contains(day)) {
                return OpeningStatus.CONFLICT;
            }

            List<TimeWindow> windows =
                    weeklyHours.get(day);

            if (windows == null || windows.isEmpty()) {
                return OpeningStatus.UNKNOWN;
            }

            LocalTime start =
                    activity.getStartTime();

            // Overnight visits are not supported
            // by this basic weekly-hours checker.
            if (activity.getDurationMinutes()
                    >= 24 * 60) {
                return OpeningStatus.UNKNOWN;
            }

            LocalTime end =
                    start.plusMinutes(
                            activity.getDurationMinutes());

            if (!end.isAfter(start)) {
                return OpeningStatus.UNKNOWN;
            }

            for (TimeWindow window : windows) {

                if (window.contains(start, end)) {
                    return OpeningStatus.FITS_HOURS;
                }
            }

            return OpeningStatus.CONFLICT;
        }
    }

    // Original method: detects overlapping activities.
    public boolean checkConflicts(Itinerary itinerary) {

        validateItinerary(itinerary);

        List<Activity> activities =
                itinerary.getActivities();

        for (int i = 0; i < activities.size(); i++) {

            for (int j = i + 1;
                    j < activities.size();
                    j++) {

                if (activitiesOverlap(
                        activities.get(i),
                        activities.get(j))) {

                    return true;
                }
            }
        }

        return false;
    }

    // Original method: explains overlap conflicts.
    public List<String> getConflictDetails(
            Itinerary itinerary) {

        validateItinerary(itinerary);

        List<String> conflicts = new ArrayList<>();

        List<Activity> activities =
                itinerary.getActivities();

        for (int i = 0; i < activities.size(); i++) {

            for (int j = i + 1;
                    j < activities.size();
                    j++) {

                Activity first = activities.get(i);
                Activity second = activities.get(j);

                if (activitiesOverlap(first, second)) {

                    conflicts.add(
                            "Conflict: "
                            + first.getName()
                            + " overlaps with "
                            + second.getName()
                            + " on "
                            + first.getDate());
                }
            }
        }

        return conflicts;
    }

    // Original overlap logic preserved.
    private boolean activitiesOverlap(
            Activity first,
            Activity second) {

        LocalDate firstDate = first.getDate();
        LocalDate secondDate = second.getDate();

        LocalTime firstStart = first.getStartTime();
        LocalTime secondStart = second.getStartTime();

        int firstDuration = first.getDurationMinutes();
        int secondDuration = second.getDurationMinutes();

        // Incomplete legacy activities cannot
        // be checked for an overlap.
        if (firstDate == null || secondDate == null
                || firstStart == null || secondStart == null
                || firstDuration <= 0
                || secondDuration <= 0) {

            return false;
        }

        LocalDateTime firstBeginning =
                LocalDateTime.of(
                        firstDate, firstStart);

        LocalDateTime secondBeginning =
                LocalDateTime.of(
                        secondDate, secondStart);

        LocalDateTime firstEnding =
                firstBeginning.plusMinutes(
                        firstDuration);

        LocalDateTime secondEnding =
                secondBeginning.plusMinutes(
                        secondDuration);

        return firstBeginning.isBefore(secondEnding)
                && secondBeginning.isBefore(firstEnding);
    }

    // Original incomplete-schedule check.
    public boolean hasIncompleteSchedule(
            Itinerary itinerary) {

        validateItinerary(itinerary);

        for (Activity activity :
                itinerary.getActivities()) {

            if (activity.getDate() == null
                    || activity.getStartTime() == null
                    || activity.getDurationMinutes() <= 0) {

                return true;
            }
        }

        return false;
    }

    // Builds a consistent lookup key.
    // Used when storing external opening-hours data.
    public static String openingHoursKey(
            String destination,
            String attractionName) {

        return normalize(destination)
                + "|"
                + normalize(attractionName);
    }

    // Checks an activity against supplied
    // weekly opening-hours information.
    public OpeningStatus checkOpeningHours(
            Activity activity,
            Map<String, OpeningHours> openingHoursData) {

        if (activity == null) {
            throw new IllegalArgumentException(
                    "Activity cannot be null.");
        }

        if (openingHoursData == null) {
            throw new IllegalArgumentException(
                    "Opening-hours data cannot be null.");
        }

        String key = openingHoursKey(
                activity.getLocation(),
                activity.getName());

        OpeningHours hours =
                openingHoursData.get(key);

        if (hours == null) {
            return OpeningStatus.UNKNOWN;
        }

        return hours.assess(activity);
    }

    // True only when supplied opening-hours
    // data indicates an actual conflict.
    public boolean hasOpeningHoursConflicts(
            Itinerary itinerary,
            Map<String, OpeningHours> openingHoursData) {

        validateItinerary(itinerary);

        for (Activity activity :
                itinerary.getActivities()) {

            if (checkOpeningHours(
                    activity,
                    openingHoursData)
                    == OpeningStatus.CONFLICT) {

                return true;
            }
        }

        return false;
    }

    // Generate a readable opening-hours report.
    public String getOpeningHoursReport(
            Itinerary itinerary,
            Map<String, OpeningHours> openingHoursData) {

        validateItinerary(itinerary);

        if (openingHoursData == null) {
            throw new IllegalArgumentException(
                    "Opening-hours data cannot be null.");
        }

        StringBuilder report = new StringBuilder();

        report.append(
                "=== OPENING HOURS REVIEW ===\n");

        int conflicts = 0;
        int unknown = 0;
        int matching = 0;

        for (Activity activity :
                itinerary.getActivities()) {

            OpeningStatus status =
                    checkOpeningHours(
                            activity,
                            openingHoursData);

            String activityName = activity.getName();

            if (status == OpeningStatus.UNKNOWN) {

                unknown++;

                report.append("- ")
                        .append(activityName)
                        .append(": Opening hours unverified.\n");

            } else if (status == OpeningStatus.CONFLICT) {

                conflicts++;

                report.append("- WARNING: ")
                        .append(activityName)
                        .append(" may be closed during ")
                        .append(activity.getDate())
                        .append(" at ")
                        .append(activity.getStartTime())
                        .append(".\n");

            } else {

                matching++;

                report.append("- ")
                        .append(activityName)
                        .append(": Fits supplied weekly hours.\n");
            }
        }

        report.append("\nFits supplied hours: ")
                .append(matching);

        report.append("\nOpening-hours conflicts: ")
                .append(conflicts);

        report.append("\nUnverified opening hours: ")
                .append(unknown);

        report.append(
                "\nNote: Weekly opening hours may not "
                + "include holidays, special events, "
                + "or temporary closures.\n");

        report.append(
                "Opening-hours data must be obtained "
                + "from a reliable source; this class "
                + "does not retrieve it automatically.\n");

        return report.toString();
    }

    private void validateItinerary(
            Itinerary itinerary) {

        if (itinerary == null) {
            throw new IllegalArgumentException(
                    "Itinerary cannot be null.");
        }
    }

    private static String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT)
                .replaceAll("\\([^)]*\\)", " ")
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
