package com.tripgoai;

public interface PlanningStrategy {

    Itinerary createPlan(TripRequest request);

}