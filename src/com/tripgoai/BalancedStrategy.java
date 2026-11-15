
package com.tripgoai;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BalancedStrategy implements PlanningStrategy {

    private final List<Activity> availableActivities;

    // Original constructor for backward compatibility.
    public BalancedStrategy() {
        this.availableActivities = new ArrayList<>();
    }

    // Accepts activities in AI-recommended order.
    public BalancedStrategy(List<Activity> availableActivities) {

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

        Itinerary itinerary = new Itinerary(2);

        List<Activity> candidates =
                new ArrayList<>(availableActivities);

        // Preserve original AI rankings.
        // Earlier candidates have higher priority.
        Map<Activity, Integer> originalRanks =
                new HashMap<>();

        for (int i = 0; i < candidates.size(); i++) {
            originalRanks.put(candidates.get(i), i);
        }

        Set<String> usedCategories = new HashSet<>();

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

                Activity best = null;
                int bestScore = Integer.MIN_VALUE;

                for (Activity candidate : candidates) {

                    // Documented prices and AI estimates
                    // are considered during planning.
                    //
                    // Unknown costs remain eligible,
                    // but the final budget is unconfirmed.
                    if (candidate.hasKnownCost()
                            && candidate.getEstimatedCost()
                                    > remainingBudget) {
                        continue;
                    }

                    // STEP 1 - Match traveler interests.
                    int score = calculateInterestScore(
                            candidate, request);

                    // STEP 2 - Reward AI ranking.
                    int originalRank =
                            originalRanks.get(candidate);

                    int aiRankingBonus =
                            Math.max(0, 12 - originalRank);

                    score += aiRankingBonus;

                    // STEP 3 - Encourage activity variety.
                    String category =
                            candidate.getCategory()
                                    .toLowerCase(Locale.ROOT);

                    if (!usedCategories.contains(category)) {
                        score += 3;
                    }

                    // STEP 4 - Prefer available price
                    // information over unknown costs.
                    if (candidate.hasKnownCost()) {
                        score += 1;
                    }

                    // STEP 5 - Select the best activity.
                    if (best == null
                            || score > bestScore
                            || (score == bestScore
                                    && isCheaper(candidate, best))) {

                        best = candidate;
                        bestScore = score;
                    }
                }

                if (best == null) {
                    break;
                }

                int duration =
                        best.getDurationMinutes() > 0
                                ? best.getDurationMinutes()
                                : 90;

                // Fixed time slots are three hours apart.
                duration = Math.min(duration, 180);

                // Preserve the original price,
                // price status, and price source.
                Activity scheduled = new Activity(
                        nextActivityId++,
                        best.getName(),
                        best.getLocation(),
                        currentDate,
                        slot,
                        duration,
                        best.getEstimatedCost(),
                        best.getCategory(),
                        best.isOutdoor(),
                        best.getCostStatus(),
                        best.getCostSource()
                );

                itinerary.addActivity(scheduled);

                // Reserve the available budget against
                // numeric prices, including AI estimates.
                //
                // An AI estimate is not a verified cost.
                if (best.hasKnownCost()) {
                    remainingBudget -=
                            best.getEstimatedCost();
                }

                usedCategories.add(
                        best.getCategory()
                                .toLowerCase(Locale.ROOT));

                // Prevent duplicate scheduling.
                candidates.remove(best);
            }

            currentDate = currentDate.plusDays(1);
        }

        if (itinerary.getActivities().isEmpty()) {
            throw new IllegalStateException(
                    "No suitable activities could be scheduled.");
        }

        return itinerary;
    }

    // Match activity names and categories
    // against traveler interests.
    private int calculateInterestScore(
            Activity activity,
            TripRequest request) {

        int score = 0;

        String name =
                activity.getName()
                        .toLowerCase(Locale.ROOT);

        String category =
                activity.getCategory()
                        .toLowerCase(Locale.ROOT);

        for (String interest : request.getInterests()) {

            if (interest == null || interest.isBlank()) {
                continue;
            }

            String keyword =
                    interest.trim()
                            .toLowerCase(Locale.ROOT);

            if (name.contains(keyword)) {
                score += 5;
            }

            if (category.contains(keyword)) {
                score += 5;
            }
        }

        return score;
    }

    // Compare numeric prices without assuming
    // that an unknown price means free admission.
    private boolean isCheaper(
            Activity first,
            Activity second) {

        if (!first.hasKnownCost()) {
            return false;
        }

        if (!second.hasKnownCost()) {
            return true;
        }

        return first.getEstimatedCost()
                < second.getEstimatedCost();
    }
}
