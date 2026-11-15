
package com.tripgoai;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TripRequest {

    private final String destination;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final double maxBudget;

    private final List<String> interests;
    private final String dietaryRestrictions;
    private final String accommodationPreference;
    private final String transportationPreference;
    private final String travelPace;

    // Original constructor - backward compatible
    public TripRequest(String destination,
                       LocalDate startDate,
                       LocalDate endDate,
                       double maxBudget) {

        this(destination, startDate, endDate, maxBudget,
             Collections.emptyList(), "None",
             "Any", "Any", "Balanced");
    }

    // Extended constructor with travel preferences
    public TripRequest(String destination,
                       LocalDate startDate,
                       LocalDate endDate,
                       double maxBudget,
                       List<String> interests,
                       String dietaryRestrictions,
                       String accommodationPreference,
                       String transportationPreference,
                       String travelPace) {

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                "Destination is required.");
        }

        if (startDate == null || endDate == null
                || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                "Invalid travel dates.");
        }

        if (!Double.isFinite(maxBudget) || maxBudget <= 0) {
            throw new IllegalArgumentException(
                "Budget must be a positive finite number.");
        }

        if (interests == null) {
            throw new IllegalArgumentException(
                "Interests cannot be null.");
        }

        this.destination = destination.trim();
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxBudget = maxBudget;

        this.interests = new ArrayList<>();

        for (String interest : interests) {
            if (interest != null && !interest.isBlank()) {
                this.interests.add(interest.trim());
            }
        }

        this.dietaryRestrictions =
            normalize(dietaryRestrictions, "None");

        this.accommodationPreference =
            normalize(accommodationPreference, "Any");

        this.transportationPreference =
            normalize(transportationPreference, "Any");

        this.travelPace =
            normalize(travelPace, "Balanced");
    }

    private String normalize(String value, String defaultValue) {
        return value == null || value.isBlank()
                ? defaultValue : value.trim();
    }

    public String getDestination() {
        return destination;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getMaxBudget() {
        return maxBudget;
    }

    public List<String> getInterests() {
        return Collections.unmodifiableList(interests);
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public String getAccommodationPreference() {
        return accommodationPreference;
    }

    public String getTransportationPreference() {
        return transportationPreference;
    }

    public String getTravelPace() {
        return travelPace;
    }

    // Number of travel days, including arrival and departure
    public long getTripDurationDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    @Override
    public String toString() {
        return "Destination: " + destination
            + "\nStart Date: " + startDate
            + "\nEnd Date: " + endDate
            + "\nDuration: " + getTripDurationDays() + " days"
            + "\nMaximum Budget: $" + maxBudget
            + "\nInterests: " + interests
            + "\nDietary Restrictions: " + dietaryRestrictions
            + "\nAccommodation: " + accommodationPreference
            + "\nTransportation: " + transportationPreference
            + "\nTravel Pace: " + travelPace;
    }
}

