
package com.tripgoai;

import java.util.Scanner;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        System.out.println("==================================");
        System.out.println("        Welcome to TripGoAI");
        System.out.println("     AI Travel Planning Agent");
        System.out.println("==================================");

        try {

            // Register travel tools
            ToolManager toolManager = new ToolManager();

            toolManager.registerTool(new DestinationSearchTool());
            toolManager.registerTool(new AttractionSearchTool());
            toolManager.registerTool(new RestaurantSearchTool());
            toolManager.registerTool(new AccommodationSearchTool());
            toolManager.registerTool(new TransportationTool());
            toolManager.registerTool(new WeatherTool());

            System.out.println(
                    "\nRegistered travel tools: "
                    + toolManager.getToolCount());

            // Adapter Pattern: OpenAI integration
            LLMClient llmClient =
                    new OpenAIAdapter("gpt-4o-mini");

            // Memory component
            
            
            MemoryManager memoryManager = MemoryManager.getInstance();

            // Strategy Pattern:
            // Actual activity candidates are supplied
            // when an itinerary is generated.
            TripPlanner tripPlanner =
                    new TripPlanner(new BalancedStrategy());

            // Travel agent
            TravelAgent travelAgent =
                    new TravelAgent(
                            toolManager,
                            llmClient,
                            memoryManager,
                            tripPlanner);

            // Facade Pattern
            TravelAgentController controller =
                    new TravelAgentController(travelAgent);

            System.out.println(
                    "TripGoAI initialized successfully.");

            System.out.println("\nChoose interface:");
            System.out.println("1. Graphical User Interface");
            System.out.println("2. Command Line Interface");
            System.out.println("3. Both GUI and CLI");

            System.out.print("\nEnter choice (1-3): ");

            Scanner scanner = new Scanner(System.in);

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    launchGUI(controller);
                    break;

                case "2":
                    launchCLI(controller);
                    break;

                case "3":
                    launchGUI(controller);
                    launchCLI(controller);
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Starting GUI.");

                    launchGUI(controller);
                    break;
            }

        } catch (RuntimeException e) {

            System.err.println(
                    "TripGoAI startup failed: "
                    + e.getMessage());

            e.printStackTrace();
        }
    }

    private static void launchGUI(
            TravelAgentController controller) {

        SwingUtilities.invokeLater(() -> {

            TravelPlannerGUI gui =
                    new TravelPlannerGUI(controller);

            gui.display();
        });
    }

    private static void launchCLI(
            TravelAgentController controller) {

        CommandLineInterface cli =
                new CommandLineInterface(controller);

        cli.start();
    }
}
