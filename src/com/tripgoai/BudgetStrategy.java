
package com.tripgoai;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetStrategy implements PlanningStrategy {

    private final List<Activity> availableActivities;

    // Original constructor for compatibility.
    public BudgetStrategy() {
        this.availableActivities = new ArrayList<>();
    }

    // Accepts AI-ranked or manually supplied activities.
    public BudgetStrategy(List<Activity> availableActivities) {

        if (availableActivities == null) {
            throw new IllegalArgumentException(
                    "Available activities cannot be null.");
        }

        this.availableActivities =
                new ArrayList<>(availableActivities);
    }

    @Override
    public Itinerary createPlan(TripRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Trip request cannot be null.");
        }

        if (availableActivities.isEmpty()) {
            throw new IllegalStateException(
                    "No activity recommendations available. "
                    + "Retrieve destination activities first.");
        }

        Itinerary itinerary = new Itinerary(1);

        List<Activity> candidates =
                new ArrayList<>(availableActivities);

        // Preserve original AI ranking.
        // Lower index means a higher AI recommendation.
        Map<Activity, Integer> originalRanks =
                new HashMap<>();

        for (int i = 0; i < candidates.size(); i++) {
            originalRanks.put(candidates.get(i), i);
        }

        // Prioritize lower estimated admission costs.
        // Unknown costs are placed last.
        //
        // A numeric AI estimate is useful for planning,
        // but it is NOT considered a verified price.
        candidates.sort(
                Comparator
                    .comparingDouble(
                        (Activity activity) ->
                            activity.hasKnownCost()
                                ? activity.getEstimatedCost()
                                : Double.POSITIVE_INFINITY
                    )
                    .thenComparingInt(
                        activity ->
                            originalRanks.get(activity)
                    )
        );

        double remainingBudget = request.getMaxBudget();

        LocalDate currentDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        LocalTime[] timeSlots = {
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                LocalTime.of(15, 0)
        };

        int nextActivityId = 1;

        while (!currentDate.isAfter(endDate)
                && !candidates.isEmpty()) {

            for (LocalTime slot : timeSlots) {

                Activity selected = null;

                for (Activity candidate : candidates) {

                    // Use documented prices and AI estimates
                    // when comparing against the budget.
                    //
                    // Unknown costs may still be scheduled,
                    // but the budget cannot be fully confirmed.
                    if (!candidate.hasKnownCost()
                            || candidate.getEstimatedCost()
                                    <= remainingBudget) {

                        selected = candidate;
                        break;
                    }
                }

                if (selected == null) {
                    break;
                }

                int duration =
                        selected.getDurationMinutes() > 0
                            ? selected.getDurationMinutes()
                            : 90;

                // Fixed time slots are three hours apart.
                duration = Math.min(duration, 180);

                // Preserve cost amount, status, and source.
                Activity scheduled = new Activity(
                        nextActivityId++,
                        selected.getName(),
                        selected.getLocation(),
                        currentDate,
                        slot,
                        duration,
                        selected.getEstimatedCost(),
                        selected.getCategory(),
                        selected.isOutdoor(),
                        selected.getCostStatus(),
                        selected.getCostSource()
                );

                itinerary.addActivity(scheduled);

                // Reserve budget for numeric costs,
                // including unverified AI estimates.
                //
                // This is a planning calculation,
                // not proof that the actual price
                // is within the budget.
                if (selected.hasKnownCost()) {
                    remainingBudget -=
                            selected.getEstimatedCost();
                }

                // Prevent duplicate scheduling.
                candidates.remove(selected);
            }

            currentDate = currentDate.plusDays(1);
        }

        if (itinerary.getActivities().isEmpty()) {
            throw new IllegalStateException(
                    "No suitable activities could be scheduled.");
        }

        return itinerary;
    }
}
