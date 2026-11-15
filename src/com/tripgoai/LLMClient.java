package com.tripgoai;

public interface LLMClient {

    String generate(String prompt);

    TripRequest extractTripRequest(String text);
}

