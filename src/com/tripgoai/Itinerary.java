
package com.tripgoai;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Itinerary implements Serializable {

    private static final long serialVersionUID = 1L;

    private int itineraryId;
    private List<Activity> activities;

    public Itinerary(int itineraryId) {
        this.itineraryId = itineraryId;
        this.activities = new ArrayList<>();
    }

    public int getItineraryId() {
        return itineraryId;
    }

    public void addActivity(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException(
                "Activity cannot be null.");
        }

        activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public List<Activity> getActivities() {
        return Collections.unmodifiableList(activities);
    }

    // Return activities sorted by date and start time
    public List<Activity> getSortedActivities() {
        List<Activity> sorted = new ArrayList<>(activities);

        sorted.sort(
            Comparator.comparing(
                Activity::getDate,
                Comparator.nullsLast(
                    Comparator.naturalOrder()))
            .thenComparing(
                Activity::getStartTime,
                Comparator.nullsLast(
                    Comparator.naturalOrder()))
        );

        return sorted;
    }

    // Group scheduled activities by travel date
    public Map<LocalDate, List<Activity>> getActivitiesByDate() {
        Map<LocalDate, List<Activity>> grouped =
            new LinkedHashMap<>();

        for (Activity activity : getSortedActivities()) {
            LocalDate date = activity.getDate();

            if (date != null) {
                grouped.computeIfAbsent(
                    date, key -> new ArrayList<>()
                ).add(activity);
            }
        }

        return grouped;
    }

    // Sum known costs only
    public double getTotalCost() {
        double total = 0;

        for (Activity activity : activities) {
            if (activity.hasKnownCost()) {
                total += activity.getEstimatedCost();
            }
        }

        return total;
    }

    // Check whether any activity has an unknown cost
    public boolean hasUnknownCosts() {
        for (Activity activity : activities) {
            if (!activity.hasKnownCost()) {
                return true;
            }
        }

        return false;
    }

    // Calculate expenses by category
    public Map<String, Double> getCostByCategory() {
        Map<String, Double> costs = new LinkedHashMap<>();

        for (Activity activity : activities) {
            if (activity.hasKnownCost()) {
                String category = activity.getCategory();

                costs.put(
                    category,
                    costs.getOrDefault(category, 0.0)
                        + activity.getEstimatedCost()
                );
            }
        }

        return costs;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("Trip Itinerary\n");
        result.append("--------------------\n");

        if (activities.isEmpty()) {
            result.append("No activities planned yet.\n");
        } else {
            LocalDate currentDate = null;
            boolean unscheduledHeaderShown = false;

            for (Activity activity : getSortedActivities()) {
                LocalDate date = activity.getDate();

                if (date != null && !date.equals(currentDate)) {
                    result.append("\nDate: ")
                          .append(date)
                          .append("\n");
                    currentDate = date;
                }

                if (date == null && !unscheduledHeaderShown) {
                    result.append("\nUnscheduled Activities:\n");
                    unscheduledHeaderShown = true;
                }

                result.append(activity).append("\n");
            }
        }

        result.append("--------------------\n");
        
        
        if (hasUnknownCosts()) {
            result.append("\nSome activity costs are unknown.");
            result.append("\nActual total may be higher.");
        }

        return result.toString();
    }
}

