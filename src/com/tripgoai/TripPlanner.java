
package com.tripgoai;

public class TripPlanner {

    private PlanningStrategy strategy;

    public TripPlanner(PlanningStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(PlanningStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException(
                "Planning strategy cannot be null."
            );
        }
        this.strategy = strategy;
    }

    public Itinerary createItinerary(TripRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                "Trip request cannot be null."
            );
        }

        return strategy.createPlan(request);
    }
}
