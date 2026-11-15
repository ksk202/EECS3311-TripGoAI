
package com.tripgoai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BudgetManager {

    private double totalCost;
    private double documentedCost;
    private double aiEstimatedCost;
    private double unspecifiedCost;

    private boolean unknownCosts;
    private boolean unverifiedCosts;
    private boolean calculated;

    private int documentedCount;
    private int aiEstimateCount;
    private int unspecifiedCount;
    private int unknownCount;

    // Calculate total activity costs.
    public double calculateTotal(Itinerary itinerary) {

        if (itinerary == null) {
            throw new IllegalArgumentException(
                    "Itinerary cannot be null.");
        }

        totalCost = 0.0;
        documentedCost = 0.0;
        aiEstimatedCost = 0.0;
        unspecifiedCost = 0.0;

        documentedCount = 0;
        aiEstimateCount = 0;
        unspecifiedCount = 0;
        unknownCount = 0;

        unknownCosts = false;
        unverifiedCosts = false;
        calculated = false;

        for (Activity activity : itinerary.getActivities()) {

            if (activity == null) {
                throw new IllegalArgumentException(
                        "Null activity in itinerary.");
            }

            if (!activity.hasKnownCost()) {
                unknownCount++;
                unknownCosts = true;
                continue;
            }

            double amount = activity.getEstimatedCost();

            if (!Double.isFinite(amount) || amount < 0) {
                throw new IllegalArgumentException(
                        "Invalid price for "
                        + activity.getName());
            }

            totalCost += amount;

            switch (activity.getCostStatus()) {

                case DOCUMENTED:
                    documentedCost += amount;
                    documentedCount++;
                    break;

                case AI_ESTIMATE:
                    aiEstimatedCost += amount;
                    aiEstimateCount++;
                    unverifiedCosts = true;
                    break;

                case UNSPECIFIED:
                    unspecifiedCost += amount;
                    unspecifiedCount++;
                    unverifiedCosts = true;
                    break;

                case UNKNOWN:
                    unknownCount++;
                    unknownCosts = true;
                    break;
            }
        }

        if (!Double.isFinite(totalCost)) {
            throw new IllegalStateException(
                    "Calculated activity cost is invalid.");
        }

        calculated = true;
        return totalCost;
    }

    // Check whether costs are confirmed within budget.
    public boolean isWithinBudget(double limit) {

        validateLimit(limit);
        requireCalculation();

        if (unknownCosts || unverifiedCosts) {
            return false;
        }

        return totalCost <= limit;
    }

    // Check whether available costs exceed the budget.
    public boolean exceedsBudget(double limit) {

        validateLimit(limit);
        requireCalculation();

        return totalCost > limit;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public double getDocumentedCost() {
        return documentedCost;
    }

    public double getAIEstimatedCost() {
        return aiEstimatedCost;
    }

    public double getUnspecifiedCost() {
        return unspecifiedCost;
    }

    public boolean hasUnknownCosts() {
        return unknownCosts;
    }

    public boolean hasUnverifiedCosts() {
        return unverifiedCosts;
    }

    public int getDocumentedCount() {
        return documentedCount;
    }

    public int getAIEstimateCount() {
        return aiEstimateCount;
    }

    public int getUnspecifiedCount() {
        return unspecifiedCount;
    }

    public int getUnknownCount() {
        return unknownCount;
    }

    // Calculate remaining budget using available costs.
    public double getRemainingBudget(double limit) {

        validateLimit(limit);
        requireCalculation();

        return limit - totalCost;
    }

    // Simplified Budget Report: budget only.
    public String getBudgetReport(double limit) {

        validateLimit(limit);
        requireCalculation();

        return "Budget: " + money(limit);
    }

    // Audit all activity prices.
    public static PriceAuditResult auditPrices(
            Itinerary itinerary,
            double budget) {

        if (itinerary == null) {
            throw new IllegalArgumentException(
                    "Itinerary cannot be null.");
        }

        if (!Double.isFinite(budget) || budget < 0) {
            throw new IllegalArgumentException(
                    "Invalid budget.");
        }

        double knownCost = 0.0;
        double documentedCost = 0.0;
        double aiEstimatedCost = 0.0;
        double unspecifiedCost = 0.0;

        int documentedCount = 0;

        List<String> unknown = new ArrayList<>();
        List<String> aiEstimated = new ArrayList<>();
        List<String> unspecified = new ArrayList<>();

        for (Activity activity : itinerary.getActivities()) {

            if (activity == null) {
                throw new IllegalArgumentException(
                        "Null activity in itinerary.");
            }

            if (!activity.hasKnownCost()) {
                unknown.add(activity.getName());
                continue;
            }

            double cost = activity.getEstimatedCost();

            if (!Double.isFinite(cost) || cost < 0) {
                throw new IllegalArgumentException(
                        "Invalid price for "
                        + activity.getName());
            }

            knownCost += cost;

            switch (activity.getCostStatus()) {

                case DOCUMENTED:
                    documentedCost += cost;
                    documentedCount++;
                    break;

                case AI_ESTIMATE:
                    aiEstimatedCost += cost;
                    aiEstimated.add(activity.getName());
                    break;

                case UNSPECIFIED:
                    unspecifiedCost += cost;
                    unspecified.add(activity.getName());
                    break;

                case UNKNOWN:
                    unknown.add(activity.getName());
                    break;
            }
        }

        if (!Double.isFinite(knownCost)) {
            throw new IllegalStateException(
                    "Activity total is not finite.");
        }

        return new PriceAuditResult(
                budget,
                knownCost,
                documentedCost,
                aiEstimatedCost,
                unspecifiedCost,
                unknown,
                aiEstimated,
                unspecified,
                documentedCount);
    }

    // Holds price audit results.
    public static final class PriceAuditResult {

        private final double budget;
        private final double knownCost;
        private final double documentedCost;
        private final double aiEstimatedCost;
        private final double unspecifiedCost;
        private final int documentedCount;

        private final List<String> unknownActivities;
        private final List<String> aiEstimatedActivities;
        private final List<String> unspecifiedActivities;

        private PriceAuditResult(
                double budget,
                double knownCost,
                double documentedCost,
                double aiEstimatedCost,
                double unspecifiedCost,
                List<String> unknownActivities,
                List<String> aiEstimatedActivities,
                List<String> unspecifiedActivities,
                int documentedCount) {

            this.budget = budget;
            this.knownCost = knownCost;
            this.documentedCost = documentedCost;
            this.aiEstimatedCost = aiEstimatedCost;
            this.unspecifiedCost = unspecifiedCost;
            this.documentedCount = documentedCount;

            this.unknownActivities =
                    Collections.unmodifiableList(
                            new ArrayList<>(unknownActivities));

            this.aiEstimatedActivities =
                    Collections.unmodifiableList(
                            new ArrayList<>(aiEstimatedActivities));

            this.unspecifiedActivities =
                    Collections.unmodifiableList(
                            new ArrayList<>(unspecifiedActivities));
        }

        public double getKnownCost() {
            return knownCost;
        }

        public double getRemainingAfterKnownCosts() {
            return budget - knownCost;
        }

        public int getUnknownPriceCount() {
            return unknownActivities.size();
        }

        public List<String> getUnknownActivities() {
            return unknownActivities;
        }

        public double getDocumentedCost() {
            return documentedCost;
        }

        public double getAIEstimatedCost() {
            return aiEstimatedCost;
        }

        public double getUnspecifiedCost() {
            return unspecifiedCost;
        }

        public int getDocumentedPriceCount() {
            return documentedCount;
        }

        public int getAIEstimateCount() {
            return aiEstimatedActivities.size();
        }

        public int getUnspecifiedPriceCount() {
            return unspecifiedActivities.size();
        }

        public List<String> getAIEstimatedActivities() {
            return aiEstimatedActivities;
        }

        public List<String> getUnspecifiedActivities() {
            return unspecifiedActivities;
        }

        public boolean hasUnverifiedPrices() {
            return !aiEstimatedActivities.isEmpty()
                    || !unspecifiedActivities.isEmpty();
        }

        public boolean isBudgetConfirmed() {
            return unknownActivities.isEmpty()
                    && !hasUnverifiedPrices()
                    && knownCost <= budget;
        }

        // Existing status logic preserved internally.
        public String getStatus() {

            if (documentedCost > budget) {
                return "OVER BUDGET";
            }

            if (knownCost > budget) {
                return "ESTIMATED OVER BUDGET";
            }

            if (!unknownActivities.isEmpty()) {
                return "BUDGET UNCONFIRMED";
            }

            if (hasUnverifiedPrices()) {
                return "ESTIMATED WITHIN BUDGET";
            }

            return "WITHIN BUDGET";
        }

        // Simplified Activity Price Audit.

public String toReport() {

    StringBuilder report = new StringBuilder();

    report.append(
            "=== ACTIVITY PRICE AUDIT ===\n\n");

    report.append(String.format(
            Locale.US,
            "Total: $%.2f%n",
            knownCost));

    report.append(String.format(
            Locale.US,
            "Remaining Balance: $%.2f%n",
            getRemainingAfterKnownCosts()));

    return report.toString();
}

    }

    // Validate budget input.
    private void validateLimit(double limit) {

        if (!Double.isFinite(limit) || limit < 0) {
            throw new IllegalArgumentException(
                    "Budget must be a non-negative "
                    + "finite number.");
        }
    }

    // Ensure activity costs have been calculated.
    private void requireCalculation() {

        if (!calculated) {
            throw new IllegalStateException(
                    "Calculate the itinerary cost first.");
        }
    }

    // Format currency.
    private String money(double value) {
        return String.format(Locale.US, "$%.2f", value);
    }
}
