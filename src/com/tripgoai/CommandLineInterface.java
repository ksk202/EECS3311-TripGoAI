
package com.tripgoai;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class CommandLineInterface {

    private final TravelAgentController controller;
    private final Scanner scanner;

    private Trip currentTrip;
    private TripRequest currentRequest;

    public CommandLineInterface(
            TravelAgentController controller) {

        if (controller == null) {
            throw new IllegalArgumentException(
                    "Controller cannot be null.");
        }

        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void start() {

        boolean running = true;

        while (running) {

            printMenu();

            String choice;

            try {
                choice = scanner.nextLine().trim();
            } catch (java.util.NoSuchElementException e) {
                System.out.println("\nInput closed.");
                break;
            }

            try {
                switch (choice) {

                    case "1":
                        researchDestination();
                        break;

                    case "2":
                        askAI();
                        break;

                    case "3":
                        interpretNaturalLanguage();
                        break;

                    case "4":
                        generateItinerary();
                        break;

                    case "5":
                        savePreference();
                        break;

                    case "6":
                        getPreference();
                        break;

                    case "7":
                        checkWeather();
                        break;

                    case "8":
                        reviewWeather();
                        break;

                    case "9":
                        suggestAlternatives();
                        break;

                    case "10":
                        generatePackingList();
                        break;

                    case "11":
                        saveTrip();
                        break;

                    case "12":
                        loadTrip();
                        break;

                    case "13":
                        deleteTrip();
                        break;

                    case "14":
                        modifyItinerary();
                        break;

                    case "15":
                        undoItineraryEdit();
                        break;

                    case "16":
                        planTransportationRoute();
                        break;

                    case "17":
                        running = false;
                        System.out.println(
                                "Goodbye from TripGoAI!");
                        break;

                    default:
                        System.out.println(
                                "Invalid option. Try again.");
                }

            } catch (RuntimeException e) {

                System.out.println(
                        "\nUnable to complete request: "
                        + e.getMessage());
            }
        }
    }

    private void printMenu() {

        System.out.println(
                "\n========== TRIPGOAI MENU ==========");

        System.out.println("1. Research Destination");
        System.out.println("2. Ask AI");
        System.out.println("3. Interpret Natural-Language Request");
        System.out.println("4. Generate Multi-Day Itinerary");
        System.out.println("5. Save Travel Preference");
        System.out.println("6. View Travel Preference");
        System.out.println("7. Weather Forecast");
        System.out.println("8. Review Itinerary Weather");
        System.out.println("9. Suggest Alternatives");
        System.out.println("10. Generate Packing List");
        System.out.println("11. Save Trip");
        System.out.println("12. Load Trip");
        System.out.println("13. Delete Trip");
        System.out.println("14. Modify Itinerary");
        System.out.println("15. Undo Last Edit");
        System.out.println("16. Plan Transportation Route");
        System.out.println("17. Exit");

        System.out.print("\nChoose an option: ");
    }

    // F03 - Research Destination

    private void researchDestination() {

        System.out.print("Enter destination: ");

        String destination =
                scanner.nextLine().trim();

        System.out.println(
                "\nResearching destination...");

        System.out.println(
                controller.researchDestination(destination));
    }

    // Ask AI

    private void askAI() {

        System.out.print("Ask TripGoAI: ");

        String prompt = scanner.nextLine().trim();

        if (prompt.isEmpty()) {
            System.out.println(
                    "Please enter a question.");
            return;
        }

        System.out.println("\nAsking AI...");

        System.out.println(controller.askAI(prompt));
    }

    // F02 - Natural-Language Request

    private void interpretNaturalLanguage() {

        System.out.println(
                "\n=== NATURAL-LANGUAGE TRIP REQUEST ===");

        System.out.print("Describe your trip: ");

        String text = scanner.nextLine().trim();

        if (text.isEmpty()) {
            System.out.println(
                    "Enter a travel request first.");
            return;
        }

        System.out.println(
                "\nInterpreting with AI...");

        TripRequest request =
                controller.interpretTripRequest(text);

        currentRequest = request;

        System.out.println(
                "\n=== INTERPRETED REQUEST ===");

        System.out.println(request);

        System.out.println(
                "\nChoose option 4 to generate "
                + "an itinerary using these details.");
    }

    // F04 - Generate Multi-Day Itinerary

    private void generateItinerary() {

        System.out.println(
                "\n=== MULTI-DAY TRIP PLANNER ===");

        TripRequest request;

        if (currentRequest != null) {

            System.out.print(
                    "Use the previously entered trip "
                    + "request? (y/n): ");

            String answer =
                    scanner.nextLine().trim();

            if (answer.equalsIgnoreCase("y")
                    || answer.equalsIgnoreCase("yes")) {

                request = currentRequest;

            } else {
                request = readTripRequest();
            }

        } else {
            request = readTripRequest();
        }

        System.out.println("\nPlanning Strategy:");
        System.out.println("1. Budget");
        System.out.println("2. Balanced");

        System.out.print("Choose strategy: ");

        String strategy =
                scanner.nextLine().trim();

        if (!strategy.equals("1")
                && !strategy.equals("2")) {

            System.out.println(
                    "Invalid strategy. Using Balanced.");
            strategy = "2";
        }

        boolean budgetFocused =
                strategy.equals("1");

        System.out.println(
                "\nRetrieving attractions and planning...");

        Itinerary itinerary =
                controller.generateItineraryAutomatically(
                        request,
                        budgetFocused);

        int tripId =
                ThreadLocalRandom.current()
                        .nextInt(100000000, 1000000000);

        currentTrip = new Trip(
                tripId,
                request.getDestination(),
                itinerary);

        currentRequest = request;

        System.out.println(
                "\n=== GENERATED ITINERARY ===\n");

        System.out.println(currentTrip);

        printValidationReports(
                itinerary,
                request.getMaxBudget());

        System.out.println(
                "\nTrip ID: " + currentTrip.getTripId());

        
    }

    // F01 - Save Travel Preference

    private void savePreference() {

        System.out.print("Preference name: ");
        String key = scanner.nextLine().trim();

        System.out.print("Preference value: ");
        String value = scanner.nextLine().trim();

        controller.savePreference(key, value);

        System.out.println(
                "Travel preference saved successfully.");
    }

    // F01 - View Travel Preference

    private void getPreference() {

        System.out.print("Preference name: ");

        String key = scanner.nextLine().trim();

        String value =
                controller.getPreference(key);

        System.out.println(
                value == null
                ? "No saved preference found."
                : "Saved preference: " + value);
    }

    // F11 - Weather Forecast

    private void checkWeather() {

        String destination =
                readDestination();

        LocalDate date = readDate(
                "Forecast date (YYYY-MM-DD): ");

        System.out.println(
                "\nChecking weather forecast...");

        System.out.println(
                controller.getWeatherAdvice(
                        destination, date));
    }

    // F11 - Review Itinerary Weather

    private void reviewWeather() {

        if (!requireTrip()) {
            return;
        }

        System.out.println(
                "\nReviewing itinerary weather...");

        System.out.println(
                controller.reviewItineraryForWeather(
                        currentTrip.getDestination(),
                        currentTrip.getItinerary()));

        System.out.println(
                "\nUse option 11 to save any changes.");
    }

    // F13 - Suggest Alternative Itinerary

    private void suggestAlternatives() {

        if (!requireTrip()) {
            return;
        }

        System.out.print(
                "Why do you need an alternative? ");

        String reason =
                scanner.nextLine().trim();

        if (reason.isEmpty()) {
            System.out.println(
                    "Please provide a reason.");
            return;
        }

        System.out.println(
                "\nGenerating alternative suggestions...");

        System.out.println(
                controller.suggestAlternatives(
                        currentTrip.getDestination(),
                        currentTrip.getItinerary(),
                        reason));
    }

    // F14 - Generate Packing List

    private void generatePackingList() {

        TripRequest request;

        if (currentRequest != null) {

            request = currentRequest;

        } else {

            System.out.println(
                    "Enter trip details "
                    + "for the packing list.");

            request = readTripRequest();
        }

        System.out.println(
                "\nGenerating packing list...");

        System.out.println(
                controller.generatePackingList(request));
    }

    // Save Trip

    private void saveTrip() {

        if (!requireTrip()) {
            return;
        }

        controller.saveTrip(currentTrip);

        System.out.println(
                "Trip saved successfully! ID: "
                + currentTrip.getTripId());
    }

    // Load Trip

    private void loadTrip() {

        int tripId =
                readTripId("Enter Trip ID: ");

        Trip trip = controller.loadTrip(tripId);

        if (trip == null) {
            System.out.println("Trip not found.");
            return;
        }

        currentTrip = trip;
        currentRequest = null;

        System.out.println(
                "\nTrip loaded successfully!");

        System.out.println(currentTrip);
    }

    // Delete Trip

    private void deleteTrip() {

        int tripId =
                readTripId(
                        "Enter Trip ID to delete: ");

        System.out.print(
                "Are you sure? (y/n): ");

        String answer =
                scanner.nextLine().trim();

        if (!answer.equalsIgnoreCase("y")
                && !answer.equalsIgnoreCase("yes")) {

            System.out.println("Deletion cancelled.");
            return;
        }

        boolean deleted =
                controller.deleteTrip(tripId);

        if (!deleted) {
            System.out.println("Trip not found.");
            return;
        }

        if (currentTrip != null
                && currentTrip.getTripId() == tripId) {

            currentTrip = null;
            currentRequest = null;
        }

        System.out.println(
                "Trip deleted successfully!");
    }

    // F12 - Modify Itinerary Using AI

    private void modifyItinerary() {

        if (!requireTrip()) {
            return;
        }

        TripRequest request =
                getEditingRequest();

        System.out.println(
                "\n=== MODIFY ITINERARY USING AI ===");

        System.out.println(
                currentTrip.getItinerary());

        System.out.print(
                "\nDescribe your requested change: ");

        String instruction =
                scanner.nextLine().trim();

        if (instruction.isEmpty()) {
            System.out.println(
                    "An editing instruction is required.");
            return;
        }

        controller.modifyTrip(
                request,
                currentTrip.getItinerary(),
                instruction);

        currentRequest = request;

        System.out.println(
                "\n=== UPDATED ITINERARY ===");

        System.out.println(currentTrip);

        printValidationReports(
                currentTrip.getItinerary(),
                request.getMaxBudget());

        System.out.println(
                "\nUse option 11 to save your changes.");
    }

    // F12 - Undo Last Edit

    private void undoItineraryEdit() {

        if (!requireTrip()) {
            return;
        }

        if (currentRequest == null) {
            System.out.println(
                    "No active editing request is available.");
            return;
        }

        controller.undoLastEdit(
                currentRequest,
                currentTrip.getItinerary());

        System.out.println(
                "\n=== PREVIOUS ITINERARY RESTORED ===");

        System.out.println(currentTrip);

        printValidationReports(
                currentTrip.getItinerary(),
                currentRequest.getMaxBudget());
    }

    // F08 - Transportation Route Planning

    private void planTransportationRoute() {

        System.out.println(
                "\n=== TRANSPORTATION PLANNING ===");

        System.out.print("Starting location: ");

        String origin =
                scanner.nextLine().trim();

        System.out.print("Destination: ");

        String destination =
                scanner.nextLine().trim();

        if (origin.isEmpty() || destination.isEmpty()) {
            System.out.println(
                    "Both locations are required.");
            return;
        }

        System.out.println(
                "\nCalculating driving route...");

        System.out.println(
                controller.planTransportationRoute(
                        origin,
                        destination));
    }

    // Read Trip Request

    private TripRequest readTripRequest() {

        String destination =
                readDestination();

        LocalDate startDate = readDate(
                "Start date (YYYY-MM-DD): ");

        LocalDate endDate = readDate(
                "End date (YYYY-MM-DD): ");

        double budget = readBudget(
                "Maximum activity budget ($): ");

        System.out.print(
                "Interests (comma-separated, or blank): ");

        String interestsText =
                scanner.nextLine().trim();

        List<String> interests = new ArrayList<>();

        if (!interestsText.isEmpty()) {

            for (String item :
                    interestsText.split(",")) {

                if (!item.isBlank()) {
                    interests.add(item.trim());
                }
            }
        }

        System.out.print(
                "Dietary restrictions (or blank): ");
        String dietary = scanner.nextLine().trim();

        System.out.print(
                "Accommodation preference (or blank): ");
        String accommodation = scanner.nextLine().trim();

        System.out.print(
                "Transportation preference (or blank): ");
        String transportation = scanner.nextLine().trim();

        System.out.print(
                "Travel pace (or blank): ");
        String pace = scanner.nextLine().trim();

        return new TripRequest(
                destination,
                startDate,
                endDate,
                budget,
                interests,
                dietary,
                accommodation,
                transportation,
                pace);
    }

    // Build Editing Request for Loaded Trips

    private TripRequest getEditingRequest() {

        if (currentRequest != null) {
            return currentRequest;
        }

        List<Activity> activities =
                currentTrip.getItinerary().getActivities();

        if (activities.isEmpty()) {
            throw new IllegalStateException(
                    "The loaded trip has no activities.");
        }

        LocalDate firstDate = null;
        LocalDate lastDate = null;

        for (Activity activity : activities) {

            LocalDate date = activity.getDate();

            if (date == null) {
                throw new IllegalStateException(
                        "An activity has no scheduled date.");
            }

            if (firstDate == null
                    || date.isBefore(firstDate)) {
                firstDate = date;
            }

            if (lastDate == null
                    || date.isAfter(lastDate)) {
                lastDate = date;
            }
        }

        System.out.println(
                "Loaded trips do not store the "
                + "original budget preferences.");

        double budget = readBudget(
                "Enter this trip's activity budget ($): ");

        return new TripRequest(
                currentTrip.getDestination(),
                firstDate,
                lastDate,
                budget);
    }

    // Budget, Attractions, Price Audit and Schedule Reports
    
    
    
    
    
    
    
    
    
    
    
    
    

private void printValidationReports(
        Itinerary itinerary,
        double budget) {

    BudgetManager budgetManager =
            new BudgetManager();

    budgetManager.calculateTotal(itinerary);

    // Display budget only.
    System.out.println(
            "\n=== BUDGET REPORT ===\n");

    System.out.println(
            budgetManager.getBudgetReport(budget));

    // Display Activity Price Audit.
    System.out.println();

    System.out.println(
            BudgetManager.auditPrices(
                    itinerary, budget).toReport());

    // Preserve schedule validation.
    ScheduleManager scheduleManager =
            new ScheduleManager();

    System.out.println(
            "\n=== SCHEDULE REPORT ===\n");

    if (scheduleManager.checkConflicts(itinerary)) {

        for (String conflict :
                scheduleManager.getConflictDetails(
                        itinerary)) {

            System.out.println(conflict);
        }

    } else {

        System.out.println(
                "No overlapping activities detected.");
    }

    if (scheduleManager.hasIncompleteSchedule(itinerary)) {

        System.out.println(
                "WARNING: Some scheduling "
                + "information is incomplete.");
    }
}

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
    // Check Whether a Trip Exists

    private boolean requireTrip() {

        if (currentTrip == null) {

            System.out.println(
                    "Generate or load a trip first.");

            return false;
        }

        return true;
    }

    // Read Destination

    private String readDestination() {

        System.out.print("Destination: ");

        return scanner.nextLine().trim();
    }

    // Read Trip ID

    private int readTripId(String message) {

        while (true) {

            System.out.print(message);

            try {
                int id = Integer.parseInt(
                        scanner.nextLine().trim());

                if (id >= 0) {
                    return id;
                }

            } catch (NumberFormatException e) {
                // Ask again.
            }

            System.out.println(
                    "Please enter a valid Trip ID.");
        }
    }

    // Read Date

    private LocalDate readDate(String message) {

        while (true) {

            System.out.print(message);

            try {
                return LocalDate.parse(
                        scanner.nextLine().trim());

            } catch (DateTimeParseException e) {

                System.out.println(
                        "Invalid date. Use YYYY-MM-DD.");
            }
        }
    }

    // Read Budget

    private double readBudget(String message) {

        while (true) {

            System.out.print(message);

            try {
                double value = Double.parseDouble(
                        scanner.nextLine().trim());

                if (Double.isFinite(value)
                        && value > 0) {
                    return value;
                }

            } catch (NumberFormatException e) {
                // Ask again.
            }

            System.out.println(
                    "Budget must be a positive number.");
        }
    }
}
