
package com.tripgoai;

import java.io.Serializable;

public class Trip implements Serializable {

    private static final long serialVersionUID = 1L;

    private int tripId;
    private String destination;
    private Itinerary itinerary;

    public Trip(int tripId, String destination, Itinerary itinerary) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination is required.");
        }

        if (itinerary == null) {
            throw new IllegalArgumentException("Itinerary cannot be null.");
        }

        this.tripId = tripId;
        this.destination = destination;
        this.itinerary = itinerary;
    }

    public int getTripId() {
        return tripId;
    }

    public String getDestination() {
        return destination;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public double getTotalCost() {
        return itinerary.getTotalCost();
    }

    @Override
    public String toString() {
        return "Trip #" + tripId
                + "\nDestination: " + destination
                + "\n" + itinerary;
    }
}
