
package com.tripgoai;

import java.time.LocalDate;
import java.util.List;

public class TravelAgentController {

    private final TravelAgent travelAgent;
    private final TripRepository tripRepository;

    private TripRequest activeRequest;
    private Itinerary activeItinerary;

    private boolean weatherChangedAfterEdit;

    public TravelAgentController(TravelAgent travelAgent) {

        if (travelAgent == null) {
            throw new IllegalArgumentException(
                    "TravelAgent cannot be null.");
        }

        this.travelAgent = travelAgent;
        this.tripRepository = new TripRepository();
    }

    private synchronized void rememberRequest(
            TripRequest request,
            Itinerary itinerary) {

        activeRequest = request;
        activeItinerary = itinerary;
        weatherChangedAfterEdit = false;
    }

    private synchronized TripRequest getActiveRequest(
            Itinerary itinerary) {

        if (itinerary == activeItinerary) {
            return activeRequest;
        }

        return null;
    }

    private synchronized Double getActiveBudget(
            Itinerary itinerary) {

        TripRequest request = getActiveRequest(itinerary);

        return request == null
                ? null : request.getMaxBudget();
    }

    // F01 - Preferences and memory

    public void savePreference(
            String key,
            String value) {

        travelAgent.getMemoryManager()
                .savePreference(key, value);
    }

    public String getPreference(String key) {

        return travelAgent.getMemoryManager()
                .getPreference(key);
    }

    // F02 - Natural-language trip request

    public TripRequest interpretTripRequest(String text) {

        return travelAgent.interpretTripRequest(text);
    }

    // F03 - Destination research

    public String researchDestination(
            String destination) {

        return travelAgent.researchDestination(
                destination);
    }

    // AI travel questions

    public String askAI(String prompt) {

        return travelAgent.askAI(prompt);
    }

    // F04 - Itinerary generation

    public Itinerary generateItinerary(
            TripRequest request) {

        Itinerary itinerary =
                travelAgent.planTrip(request);

        rememberRequest(request, itinerary);

        return itinerary;
    }

    public Itinerary generateItinerary(
            TripRequest request,
            List<Activity> candidates,
            boolean budgetFocused) {

        Itinerary itinerary =
                travelAgent.planTripWithActivities(
                        request,
                        candidates,
                        budgetFocused);

        rememberRequest(request, itinerary);

        return itinerary;
    }

    public Itinerary generateItineraryAutomatically(
            TripRequest request,
            boolean budgetFocused) {

        Itinerary itinerary =
                travelAgent.planTripAutomatically(
                        request, budgetFocused);

        rememberRequest(request, itinerary);

        return itinerary;
    }

    // F08 - Transportation planning

    public String planTransportationRoute(
            String origin,
            String destination) {

        TransportationTool transportTool = new TransportationTool();

        return transportTool.planDrivingRoute(
                origin, destination);
    }

    // F11 - Weather forecast

    public String getWeatherAdvice(
            String destination,
            LocalDate date) {

        return travelAgent.getWeatherAdvice(
                destination, date);
    }

    // F11 - Weather-aware itinerary adjustment

    public synchronized String reviewItineraryForWeather(
            String destination,
            Itinerary itinerary) {

        TripRequest request =
                getActiveRequest(itinerary);

        WeatherAdjustmentService service =
                new WeatherAdjustmentService(travelAgent);

        WeatherAdjustmentService.Result result =
                service.adjust(
                        destination, itinerary, request);
        if (result.isModified()) {

            weatherChangedAfterEdit = true;

            travelAgent.clearLastEdit();
        }


        return result.getReport();
    }

    // F12 - Natural-language editing

    public synchronized Itinerary modifyTrip(
            TripRequest request,
            Itinerary itinerary,
            String instruction) {

        Itinerary updated = travelAgent.modifyTrip(
                request, itinerary, instruction);

        rememberRequest(request, updated);

        return updated;
    }

    // F12 - Undo previous edit

    public synchronized Itinerary undoLastEdit(
            TripRequest request,
            Itinerary itinerary) {

        if (weatherChangedAfterEdit
                && itinerary == activeItinerary) {

            throw new IllegalStateException(
                    "Weather adjustment changed this itinerary. "
                    + "The previous edit cannot safely be undone.");
        }

        Itinerary updated = travelAgent.undoLastEdit(
                request, itinerary);

        rememberRequest(request, updated);

        return updated;
    }

    // F13 - Alternative itinerary suggestions

    public String suggestAlternatives(
            String destination,
            Itinerary itinerary,
            String reason) {

        AlternativePlanService service =
                new AlternativePlanService(travelAgent);

        Double budget = getActiveBudget(itinerary);

        return service.generateAlternativeReport(
                destination,
                itinerary,
                reason,
                budget);
    }

    // F13 - Separate structured alternative trip

    public Trip generateAlternativeTrip(
            TripRequest request,
            Trip original,
            String reason) {

        if (request == null || original == null) {
            throw new IllegalArgumentException(
                    "Trip request and existing trip are required.");
        }

        if (!request.getDestination().equalsIgnoreCase(
                original.getDestination())) {
            throw new IllegalArgumentException(
                    "Trip destinations do not match.");
        }

        for (Activity activity :
                original.getItinerary().getActivities()) {

            if (activity.getDate() == null
                    || activity.getDate().isBefore(
                            request.getStartDate())
                    || activity.getDate().isAfter(
                            request.getEndDate())) {

                throw new IllegalArgumentException(
                        "An activity is outside "
                        + "the requested trip dates.");
            }
        }

        AlternativePlanService service =
                new AlternativePlanService(travelAgent);

        return service.generateAlternative(
                original,
                reason,
                request.getMaxBudget());
    }

    // F14 - Packing list

    public String generatePackingList(
            TripRequest request) {

        return travelAgent.generatePackingList(request);
    }

    // Trip persistence

    public void saveTrip(Trip trip) {
        tripRepository.saveTrip(trip);
    }

    public Trip loadTrip(int tripId) {
        return tripRepository.findTrip(tripId);
    }

    public boolean deleteTrip(int tripId) {
        return tripRepository.deleteTrip(tripId);
    }
}
