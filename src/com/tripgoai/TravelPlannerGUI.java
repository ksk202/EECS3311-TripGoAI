
package com.tripgoai;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class TravelPlannerGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private final TravelAgentController controller;

    private final JTextField destinationField;
    private final JTextField startDateField;
    private final JTextField endDateField;
    private final JTextField budgetField;
    private final JTextField interestsField;
    private final JTextField dietaryField;
    private final JTextField accommodationField;
    private final JTextField transportField;

    private final JComboBox<String> paceBox;
    private final JComboBox<String> strategyBox;

    private final JTextArea outputArea;
    private final JTextArea naturalLanguageArea;

    private final List<JButton> actionButtons =
            new ArrayList<>();

    private Trip currentTrip;
    private TripRequest currentRequest;

    private boolean busy = false;

    public TravelPlannerGUI(
            TravelAgentController controller) {

        if (controller == null) {
            throw new IllegalArgumentException(
                    "Controller cannot be null.");
        }

        this.controller = controller;

        setTitle("TripGoAI - AI Travel Planning Agent");
        setSize(1100, 780);
        setMinimumSize(new Dimension(900, 650));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(
                new BorderLayout(10, 10));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        12, 12, 12, 12));

        JPanel inputPanel = new JPanel(
                new GridLayout(10, 2, 8, 6));

        destinationField = new JTextField();

        startDateField = new JTextField(
                LocalDate.now().plusDays(1).toString());

        endDateField = new JTextField(
                LocalDate.now().plusDays(3).toString());

        budgetField = new JTextField("2000");
        interestsField = new JTextField();
        dietaryField = new JTextField("None");
        accommodationField = new JTextField("Any");
        transportField = new JTextField("Any");

        paceBox = new JComboBox<>(
                new String[]{
                    "Relaxed",
                    "Balanced",
                    "Fast"
                });

        paceBox.setSelectedItem("Balanced");

        strategyBox = new JComboBox<>(
                new String[]{
                    "Budget",
                    "Balanced"
                });

        strategyBox.setSelectedItem("Balanced");

        inputPanel.add(new JLabel("Destination:"));
        inputPanel.add(destinationField);

        inputPanel.add(
                new JLabel("Start Date (YYYY-MM-DD):"));
        inputPanel.add(startDateField);

        inputPanel.add(
                new JLabel("End Date (YYYY-MM-DD):"));
        inputPanel.add(endDateField);

        inputPanel.add(
                new JLabel("Activity Budget ($):"));
        inputPanel.add(budgetField);

        inputPanel.add(
                new JLabel("Interests (comma-separated):"));
        inputPanel.add(interestsField);

        inputPanel.add(
                new JLabel("Dietary Restrictions:"));
        inputPanel.add(dietaryField);

        inputPanel.add(
                new JLabel("Accommodation Preference:"));
        inputPanel.add(accommodationField);

        inputPanel.add(
                new JLabel("Transport Preference:"));
        inputPanel.add(transportField);

        inputPanel.add(
                new JLabel("Travel Pace:"));
        inputPanel.add(paceBox);

        inputPanel.add(
                new JLabel("Planning Strategy:"));
        inputPanel.add(strategyBox);

        JPanel tripPanel = new JPanel(
                new BorderLayout(5, 5));

        tripPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Trip Details"));

        tripPanel.add(inputPanel, BorderLayout.CENTER);

        naturalLanguageArea = new JTextArea(3, 30);
        naturalLanguageArea.setLineWrap(true);
        naturalLanguageArea.setWrapStyleWord(true);

        naturalLanguageArea.setBorder(
                BorderFactory.createTitledBorder(
                        "Natural-Language Travel Request"));

        JPanel aiPanel = new JPanel(
                new BorderLayout(5, 5));

        aiPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "AI & Planning"));

        aiPanel.add(
                new JScrollPane(naturalLanguageArea),
                BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new GridLayout(0, 4, 6, 6));

        addButton(
                buttonPanel,
                "Interpret AI Request",
                this::interpretRequest);

        addButton(
                buttonPanel,
                "Research Destination",
                this::researchDestination);

        addButton(
                buttonPanel,
                "Generate Itinerary",
                this::generateItinerary);

        addButton(
                buttonPanel,
                "Ask AI",
                this::askAI);

        addButton(
                buttonPanel,
                "Weather Forecast",
                this::showWeatherForecast);

        addButton(
                buttonPanel,
                "Review Weather",
                this::reviewWeather);

        addButton(
                buttonPanel,
                "Suggest Alternatives",
                this::suggestAlternatives);

        addButton(
                buttonPanel,
                "Packing List",
                this::generatePackingList);

        addButton(
                buttonPanel,
                "Save Preferences",
                this::savePreferences);

        addButton(
                buttonPanel,
                "Save Trip",
                this::saveTrip);

        addButton(
                buttonPanel,
                "Load Trip",
                this::loadTrip);

        addButton(
                buttonPanel,
                "Delete Trip",
                this::deleteTrip);

        // F08 - Transportation planning
        addButton(
                buttonPanel,
                "Plan Transportation",
                this::planTransportationRoute);

        // F12 - Modify itinerary
        addButton(
                buttonPanel,
                "Modify Itinerary",
                this::modifyItinerary);

        addButton(
                buttonPanel,
                "Undo Last Edit",
                this::undoItineraryEdit);

        aiPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel topPanel = new JPanel(
                new BorderLayout(8, 8));

        topPanel.add(tripPanel, BorderLayout.NORTH);
        topPanel.add(aiPanel, BorderLayout.CENTER);

        JScrollPane inputScroll =
                new JScrollPane(topPanel);

        inputScroll.setBorder(null);

        inputScroll.setPreferredSize(
                new Dimension(1000, 490));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        outputArea.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        13));

        JScrollPane outputScroll =
                new JScrollPane(outputArea);

        outputScroll.setBorder(
                BorderFactory.createTitledBorder(
                        "TripGoAI Results"));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                inputScroll,
                outputScroll);

        splitPane.setResizeWeight(0.65);

        mainPanel.add(
                splitPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void addButton(
            JPanel panel,
            String title,
            Runnable action) {

        JButton button = new JButton(title);

        button.addActionListener(e -> action.run());

        actionButtons.add(button);
        panel.add(button);
    }

    private void setBusy(boolean value) {

        busy = value;

        for (JButton button : actionButtons) {
            button.setEnabled(!busy);
        }

        setCursor(
                Cursor.getPredefinedCursor(
                        busy
                        ? Cursor.WAIT_CURSOR
                        : Cursor.DEFAULT_CURSOR));
    }

    private void runTask(
            String status,
            Supplier<String> task) {

        if (busy) {
            return;
        }

        setBusy(true);
        outputArea.setText(status);

        new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() {

                return task.get();
            }

            @Override
            protected void done() {

                setBusy(false);

                try {
                    outputArea.setText(get());

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    showError("Operation interrupted.");

                } catch (ExecutionException e) {

                    Throwable cause = e.getCause();

                    showError(
                            cause == null
                            ? e.getMessage()
                            : cause.getMessage());
                }
            }
        }.execute();
    }

    // Read the current form

    private TripRequest readTripRequest() {

        String destination =
                destinationField.getText().trim();

        LocalDate startDate = LocalDate.parse(
                startDateField.getText().trim());

        LocalDate endDate = LocalDate.parse(
                endDateField.getText().trim());

        double budget = Double.parseDouble(
                budgetField.getText().trim());

        List<String> interests = new ArrayList<>();

        String interestText =
                interestsField.getText().trim();

        if (!interestText.isBlank()) {

            Arrays.stream(interestText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(interests::add);
        }

        return new TripRequest(
                destination,
                startDate,
                endDate,
                budget,
                interests,
                dietaryField.getText().trim(),
                accommodationField.getText().trim(),
                transportField.getText().trim(),
                paceBox.getSelectedItem().toString());
    }

    private TripRequest validatedRequest() {

        try {
            return readTripRequest();

        } catch (DateTimeParseException e) {

            showError(
                    "Invalid date. Use YYYY-MM-DD.");
            return null;

        } catch (NumberFormatException e) {

            showError(
                    "Enter a valid numeric activity budget.");
            return null;

        } catch (IllegalArgumentException e) {

            showError(e.getMessage());
            return null;
        }
    }

    // F02 - Interpret natural-language request

    private void interpretRequest() {

        String text =
                naturalLanguageArea.getText().trim();

        if (text.isEmpty()) {

            showError(
                    "Enter a natural-language travel request.");
            return;
        }

        runTask(
                "AI is interpreting your request...",
                () -> {

                    TripRequest request =
                            controller.interpretTripRequest(
                                    text);

                    SwingUtilities.invokeLater(() -> {

                        populateFields(request);
                        currentRequest = request;
                    });

                    return "AI interpreted your request:\n\n"
                            + request
                            + "\n\nReview the fields and click "
                            + "Generate Itinerary.";
                });
    }

    private void populateFields(TripRequest request) {

        destinationField.setText(
                request.getDestination());

        startDateField.setText(
                request.getStartDate().toString());

        endDateField.setText(
                request.getEndDate().toString());

        budgetField.setText(
                String.valueOf(request.getMaxBudget()));

        interestsField.setText(
                String.join(", ", request.getInterests()));

        dietaryField.setText(
                request.getDietaryRestrictions());

        accommodationField.setText(
                request.getAccommodationPreference());

        transportField.setText(
                request.getTransportationPreference());

        paceBox.setSelectedItem(
                request.getTravelPace());
    }

    // F03 - Research destination

    private void researchDestination() {

        String destination =
                destinationField.getText().trim();

        if (destination.isEmpty()) {
            showError("Enter a destination first.");
            return;
        }

        runTask(
                "Researching destination...",
                () -> controller.researchDestination(
                        destination));
    }

    // F04 - Generate itinerary

    private void generateItinerary() {

        TripRequest request = validatedRequest();

        if (request == null) {
            return;
        }

        boolean budgetFocused =
                "Budget".equals(
                        strategyBox.getSelectedItem());

        runTask(
                "Retrieving attractions and planning...",
                () -> {

                    Itinerary itinerary =
                            controller
                                .generateItineraryAutomatically(
                                        request,
                                        budgetFocused);

                    int tripId =
                            ThreadLocalRandom.current()
                                    .nextInt(
                                            100000000,
                                            1000000000);

                    Trip trip = new Trip(
                            tripId,
                            request.getDestination(),
                            itinerary);

                    SwingUtilities.invokeLater(() -> {

                        currentTrip = trip;
                        currentRequest = request;
                    });

                    return "=== GENERATED ITINERARY ===\n\n"
                            + trip
                            + buildValidationReport(
                                    itinerary,
                                    request.getMaxBudget())
                            + "\n\nTrip ID: "
                            + trip.getTripId()
                            + "\nUse Save Trip to persist it.";
                });
    }

    private String buildValidationReport(
            Itinerary itinerary,
            double budget) {

        StringBuilder report = new StringBuilder();

        // Budget report
        BudgetManager budgetManager =
                new BudgetManager();

        budgetManager.calculateTotal(itinerary);

        report.append("\n\n=== BUDGET REPORT ===\n");

        report.append(
                budgetManager.getBudgetReport(budget));

        // Activity price audit
        report.append("\n\n");

        report.append(
                BudgetManager.auditPrices(
                        itinerary, budget).toReport());

        // Schedule report
        ScheduleManager scheduleManager =
                new ScheduleManager();

        report.append("\n\n=== SCHEDULE REPORT ===\n");

        if (scheduleManager.checkConflicts(itinerary)) {

            for (String conflict :
                    scheduleManager.getConflictDetails(
                            itinerary)) {

                report.append(conflict).append("\n");
            }

        } else {

            report.append(
                    "No overlapping activities detected.\n");
        }

        if (scheduleManager.hasIncompleteSchedule(
                itinerary)) {

            report.append(
                    "WARNING: Incomplete schedule data.\n");
        }

        report.append(
                "Prices, opening hours, travel times "
                + "and visit durations may be unverified.");

        return report.toString();
    }

    // Ask AI

    private void askAI() {

        String prompt =
                naturalLanguageArea.getText().trim();

        if (prompt.isEmpty()) {
            showError("Enter a question for the AI.");
            return;
        }

        runTask(
                "Asking TripGoAI...",
                () -> controller.askAI(prompt));
    }

    // F08 - Transportation planning
    // Updated: user can enter both route endpoints.

    private void planTransportationRoute() {

        // The current controller and OSRM service
        // support driving routes.
        // Do not label driving estimates as public transit.

        JTextField originInput =
                new JTextField(25);

        JTextField destinationInput =
                new JTextField(
                        destinationField.getText().trim(),
                        25);

        JComboBox<String> modeInput =
                new JComboBox<>(
                        new String[]{"Driving"});

        JPanel routePanel =
                new JPanel(
                        new GridLayout(0, 1, 4, 5));

        routePanel.add(
                new JLabel("Starting location:"));

        routePanel.add(originInput);

        routePanel.add(
                new JLabel("Destination location:"));

        routePanel.add(destinationInput);

        routePanel.add(
                new JLabel("Transportation mode:"));

        routePanel.add(modeInput);

        routePanel.add(
                new JLabel(
                        "Only driving routes are currently supported."));

        while (true) {

            int choice =
                    JOptionPane.showConfirmDialog(
                            this,
                            routePanel,
                            "Transportation Planning",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            String origin =
                    originInput.getText().trim();

            String destination =
                    destinationInput.getText().trim();

            if (origin.isEmpty()) {

                showError(
                        "Enter a starting location.");
                continue;
            }

            if (destination.isEmpty()) {

                showError(
                        "Enter a destination location.");
                continue;
            }

            if (origin.equalsIgnoreCase(destination)) {

                showError(
                        "Starting location and destination must differ.");
                continue;
            }

            final String start = origin;
            final String end = destination;

            runTask(
                    "Calculating driving route...",
                    () -> controller.planTransportationRoute(
                            start, end));

            return;
        }
    }

    // F11 - Weather forecast

    private void showWeatherForecast() {

        String destination =
                destinationField.getText().trim();

        if (destination.isEmpty()) {
            showError("Enter a destination first.");
            return;
        }

        final LocalDate date;

        try {
            date = LocalDate.parse(
                    startDateField.getText().trim());

        } catch (DateTimeParseException e) {
            showError("Invalid start date.");
            return;
        }

        runTask(
                "Checking weather...",
                () -> controller.getWeatherAdvice(
                        destination, date));
    }

    // F11 - Weather-aware itinerary review

    private void reviewWeather() {

        if (!requireTrip()) {
            return;
        }

        Trip trip = currentTrip;

        runTask(
                "Reviewing itinerary weather...",
                () -> controller.reviewItineraryForWeather(
                        trip.getDestination(),
                        trip.getItinerary()));
    }

    // F13 - Alternative itinerary

    private void suggestAlternatives() {

        if (!requireTrip()) {
            return;
        }

        String reason =
                JOptionPane.showInputDialog(
                        this,
                        "Why do you need an alternative plan?",
                        "Alternative Trip",
                        JOptionPane.QUESTION_MESSAGE);

        if (reason == null || reason.isBlank()) {
            return;
        }

        Trip trip = currentTrip;
        final String requestReason = reason.trim();

        runTask(
                "Generating alternative itinerary...",
                () -> controller.suggestAlternatives(
                        trip.getDestination(),
                        trip.getItinerary(),
                        requestReason));
    }

    // F12 - Modify itinerary

    private void modifyItinerary() {

        if (!requireTrip()) {
            return;
        }

        TripRequest request =
                getEditingRequest();

        if (request == null) {
            return;
        }

        String instruction =
                JOptionPane.showInputDialog(
                        this,
                        "Describe the itinerary change:",
                        "Modify Itinerary",
                        JOptionPane.QUESTION_MESSAGE);

        if (instruction == null
                || instruction.isBlank()) {
            return;
        }

        Trip trip = currentTrip;

        runTask(
                "Modifying itinerary using AI...",
                () -> {

                    controller.modifyTrip(
                            request,
                            trip.getItinerary(),
                            instruction.trim());

                    SwingUtilities.invokeLater(() ->
                            currentRequest = request);

                    return "=== UPDATED ITINERARY ===\n\n"
                            + trip
                            + buildValidationReport(
                                    trip.getItinerary(),
                                    request.getMaxBudget())
                            + "\n\nSave the trip "
                            + "to persist your changes.";
                });
    }

    // F12 - Undo last edit

    private void undoItineraryEdit() {

        if (!requireTrip()) {
            return;
        }

        if (currentRequest == null) {

            showError(
                    "No active editing request "
                    + "is available to undo.");
            return;
        }

        Trip trip = currentTrip;
        TripRequest request = currentRequest;

        runTask(
                "Undoing last edit...",
                () -> {

                    controller.undoLastEdit(
                            request,
                            trip.getItinerary());

                    return "=== PREVIOUS ITINERARY RESTORED ===\n\n"
                            + trip
                            + buildValidationReport(
                                    trip.getItinerary(),
                                    request.getMaxBudget());
                });
    }

    // Recover editing context from current form

    private TripRequest getEditingRequest() {

        if (currentRequest != null) {
            return currentRequest;
        }

        return validatedRequest();
    }

    // F14 - Packing list

    private void generatePackingList() {

        TripRequest request = validatedRequest();

        if (request == null) {
            return;
        }

        runTask(
                "Generating packing list...",
                () -> controller.generatePackingList(
                        request));
    }

    // F01 - Save travel preferences

    private void savePreferences() {

        try {

            saveIfPresent(
                    "interests",
                    interestsField.getText());

            saveIfPresent(
                    "dietary",
                    dietaryField.getText());

            saveIfPresent(
                    "accommodation",
                    accommodationField.getText());

            saveIfPresent(
                    "transport",
                    transportField.getText());

            saveIfPresent(
                    "pace",
                    paceBox.getSelectedItem().toString());

            JOptionPane.showMessageDialog(
                    this,
                    "Travel preferences saved.",
                    "TripGoAI",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (RuntimeException e) {

            showError(e.getMessage());
        }
    }

    private void saveIfPresent(
            String key,
            String value) {

        if (value != null && !value.isBlank()) {

            controller.savePreference(
                    key, value.trim());
        }
    }

    // Save trip

    private void saveTrip() {

        if (!requireTrip()) {
            return;
        }

        Trip trip = currentTrip;

        runTask(
                "Saving trip...",
                () -> {

                    controller.saveTrip(trip);

                    return "Trip saved successfully!\n"
                            + "Trip ID: "
                            + trip.getTripId();
                });
    }

    // Load trip

    private void loadTrip() {

        String input =
                JOptionPane.showInputDialog(
                        this,
                        "Enter Trip ID:");

        if (input == null) {
            return;
        }

        final int tripId;

        try {

            tripId =
                    Integer.parseInt(input.trim());

        } catch (NumberFormatException e) {

            showError(
                    "Enter a valid numeric Trip ID.");
            return;
        }

        runTask(
                "Loading trip...",
                () -> {

                    Trip trip =
                            controller.loadTrip(tripId);

                    if (trip == null) {
                        return "Trip not found.";
                    }

                    SwingUtilities.invokeLater(() -> {

                        currentTrip = trip;
                        currentRequest = null;

                        destinationField.setText(
                                trip.getDestination());
                    });

                    return "=== LOADED TRIP ===\n\n"
                            + trip;
                });
    }

    // Delete trip

    private void deleteTrip() {

        String input =
                JOptionPane.showInputDialog(
                        this,
                        "Enter Trip ID to delete:");

        if (input == null) {
            return;
        }

        final int tripId;

        try {

            tripId =
                    Integer.parseInt(input.trim());

        } catch (NumberFormatException e) {

            showError(
                    "Enter a valid numeric Trip ID.");
            return;
        }

        int confirmation =
                JOptionPane.showConfirmDialog(
                        this,
                        "Delete Trip #" + tripId + "?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        runTask(
                "Deleting trip...",
                () -> {

                    boolean deleted =
                            controller.deleteTrip(tripId);

                    if (deleted) {

                        SwingUtilities.invokeLater(() -> {

                            if (currentTrip != null
                                    && currentTrip.getTripId()
                                            == tripId) {

                                currentTrip = null;
                                currentRequest = null;
                            }
                        });
                    }

                    return deleted
                            ? "Trip deleted successfully."
                            : "Trip not found.";
                });
    }

    private boolean requireTrip() {

        if (currentTrip == null) {

            showError(
                    "Generate or load a trip first.");

            return false;
        }

        return true;
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message == null
                ? "An unexpected error occurred."
                : message,
                "TripGoAI",
                JOptionPane.ERROR_MESSAGE);
    }

    public void display() {
        setVisible(true);
    }
}
