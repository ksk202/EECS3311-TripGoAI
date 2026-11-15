
package com.tripgoai;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum CostStatus {
        DOCUMENTED,
        AI_ESTIMATE,
        UNSPECIFIED,
        UNKNOWN
    }

    private int activityId;
    private String name;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private int durationMinutes;
    private double estimatedCost;
    private String category;
    private boolean outdoor;

    // Pricing metadata preserved for budget calculations.
    private CostStatus costStatus;
    private String costSource;

    // Original constructor.
    public Activity(
            int activityId,
            String name,
            String location,
            LocalTime startTime,
            double estimatedCost) {

        this(
                activityId,
                name,
                location,
                null,
                startTime,
                0,
                estimatedCost,
                "Other",
                false);
    }

    // Original multi-day constructor.
    public Activity(
            int activityId,
            String name,
            String location,
            LocalDate date,
            LocalTime startTime,
            int durationMinutes,
            double estimatedCost,
            String category,
            boolean outdoor) {

        this(
                activityId,
                name,
                location,
                date,
                startTime,
                durationMinutes,
                estimatedCost,
                category,
                outdoor,
                Double.isNaN(estimatedCost)
                        ? CostStatus.UNKNOWN
                        : CostStatus.UNSPECIFIED,
                "");
    }

    // Constructor supporting documented and AI-estimated prices.
    public Activity(
            int activityId,
            String name,
            String location,
            LocalDate date,
            LocalTime startTime,
            int durationMinutes,
            double estimatedCost,
            String category,
            boolean outdoor,
            CostStatus costStatus,
            String costSource) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Activity name is required.");
        }

        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Activity location is required.");
        }

        if (durationMinutes < 0) {
            throw new IllegalArgumentException(
                    "Duration cannot be negative.");
        }

        if (Double.isInfinite(estimatedCost)
                || (estimatedCost < 0
                && !Double.isNaN(estimatedCost))) {

            throw new IllegalArgumentException(
                    "Cost must be non-negative or unknown.");
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Activity category is required.");
        }

        if (durationMinutes > 0
                && (date == null || startTime == null)) {

            throw new IllegalArgumentException(
                    "Scheduled activities require a date "
                    + "and start time.");
        }

        if (durationMinutes > 0
                && startTime.plusMinutes(durationMinutes)
                        .isBefore(startTime)) {

            throw new IllegalArgumentException(
                    "Activity cannot extend past midnight.");
        }

        if (costStatus == null) {
            throw new IllegalArgumentException(
                    "Cost status cannot be null.");
        }

        boolean amountKnown =
                Double.isFinite(estimatedCost);

        if (amountKnown
                && costStatus == CostStatus.UNKNOWN) {

            throw new IllegalArgumentException(
                    "A numeric cost cannot have UNKNOWN status.");
        }

        if (!amountKnown
                && costStatus != CostStatus.UNKNOWN) {

            throw new IllegalArgumentException(
                    "An unknown cost must have UNKNOWN status.");
        }

        this.activityId = activityId;
        this.name = name;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.estimatedCost = estimatedCost;
        this.category = category;
        this.outdoor = outdoor;
        this.costStatus = costStatus;
        this.costSource =
                costSource == null ? "" : costSource;
    }

    public int getActivityId() {
        return activityId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public LocalTime getEndTime() {

        if (startTime == null || durationMinutes <= 0) {
            return null;
        }

        return startTime.plusMinutes(durationMinutes);
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    // Checks whether a valid numeric activity cost exists.
    public boolean hasKnownCost() {
        return Double.isFinite(estimatedCost)
                && estimatedCost >= 0;
    }

    // Supports older serialized activities.
    public CostStatus getCostStatus() {

        if (!hasKnownCost()) {
            return CostStatus.UNKNOWN;
        }

        return costStatus == null
                ? CostStatus.UNSPECIFIED
                : costStatus;
    }

    public String getCostSource() {
        return costSource == null ? "" : costSource;
    }

    public boolean isDocumentedCost() {
        return hasKnownCost()
                && getCostStatus()
                        == CostStatus.DOCUMENTED;
    }

    public boolean isAIEstimatedCost() {
        return hasKnownCost()
                && getCostStatus()
                        == CostStatus.AI_ESTIMATE;
    }

    public boolean hasUnverifiedCost() {
        return hasKnownCost()
                && !isDocumentedCost();
    }

    public String getCategory() {
        return category == null ? "Other" : category;
    }

    public boolean isOutdoor() {
        return outdoor;
    }

    // Displays activity details with clean price formatting.
    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        if (date != null) {
            result.append(date).append(" | ");
        }

        result.append(
                startTime == null
                        ? "Time TBD"
                        : startTime);

        if (getEndTime() != null) {
            result.append(" - ")
                    .append(getEndTime());
        }

        result.append(" | ")
                .append(name)
                .append(" (")
                .append(location)
                .append(")");

        if (!hasKnownCost()) {
            result.append(" - Cost unknown");
        } else {
            result.append(
                    String.format(
                            Locale.US,
                            " - $%.2f",
                            estimatedCost));
        }

        return result.toString();
    }
}
