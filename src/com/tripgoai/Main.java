
package com.tripgoai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        String inputFile = null;
        boolean directCLI = false;
        boolean apiMode = false;
        int apiPort = 8080;
        boolean portSpecified = false;

        // Read command-line options.
        try {
            for (int i = 0; i < args.length; i++) {

                String option = args[i];

                if (option.equalsIgnoreCase("-cli")
                        || option.equalsIgnoreCase("-LLMAPI")) {

                    directCLI = true;

                } else if (option.equalsIgnoreCase("-input")) {

                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException(
                                "Missing filename after -input.");
                    }

                    inputFile = args[++i];
                    directCLI = true;

                } else if (option.equalsIgnoreCase("-api")) {

                    apiMode = true;

                } else if (option.equalsIgnoreCase("-port")) {

                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException(
                                "Missing port after -port.");
                    }

                    try {
                        apiPort = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                "Port must be a valid number.");
                    }

                    if (apiPort < 1 || apiPort > 65535) {
                        throw new IllegalArgumentException(
                                "Port must be between 1 and 65535.");
                    }

                    portSpecified = true;

                } else if (option.equalsIgnoreCase("-help")
                        || option.equalsIgnoreCase("--help")) {

                    printHelp();
                    return;

                } else {

                    throw new IllegalArgumentException(
                            "Unknown option: " + option);
                }
            }

            if (apiMode && directCLI) {
                throw new IllegalArgumentException(
                        "Choose either API mode or CLI mode.");
            }

            if (portSpecified && !apiMode) {
                throw new IllegalArgumentException(
                        "-port requires -api.");
            }

        } catch (IllegalArgumentException e) {

            System.err.println(e.getMessage());
            printHelp();
            return;
        }

        System.out.println("==================================");
        System.out.println("        Welcome to TripGoAI");
        System.out.println("     AI Travel Planning Agent");
        System.out.println("==================================");

        try {

            // Register travel tools.
            ToolManager toolManager = new ToolManager();

            toolManager.registerTool(
                    new DestinationSearchTool());

            toolManager.registerTool(
                    new AttractionSearchTool());

            toolManager.registerTool(
                    new RestaurantSearchTool());

            toolManager.registerTool(
                    new AccommodationSearchTool());

            toolManager.registerTool(
                    new TransportationTool());

            toolManager.registerTool(
                    new WeatherTool());

            System.out.println(
                    "\nRegistered travel tools: "
                    + toolManager.getToolCount());

            // Adapter Pattern: OpenAI integration.
            LLMClient llmClient =
                    new OpenAIAdapter("gpt-4o-mini");

            // Singleton Pattern: shared memory.
            MemoryManager memoryManager =
                    MemoryManager.getInstance();

            // Strategy Pattern: default planning strategy.
            TripPlanner tripPlanner =
                    new TripPlanner(new BalancedStrategy());

            // Travel agent.
            TravelAgent travelAgent =
                    new TravelAgent(
                            toolManager,
                            llmClient,
                            memoryManager,
                            tripPlanner);

            // Facade Pattern.
            TravelAgentController controller =
                    new TravelAgentController(travelAgent);

            System.out.println(
                    "TripGoAI initialized successfully.");

            // New: HTTP API input mode.
            if (apiMode) {

                ApiServer.start(controller, apiPort);
                return;
            }

            // Existing standalone CLI mode.
            if (directCLI) {

                if (inputFile != null) {
                    runCLIFromFile(controller, inputFile);
                } else {
                    launchCLI(controller);
                }

                return;
            }

            // Preserve original interface selection.
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

        } catch (IOException | RuntimeException e) {

            System.err.println(
                    "TripGoAI startup failed: "
                    + e.getMessage());

            e.printStackTrace();
        }
    }

    private static void runCLIFromFile(
            TravelAgentController controller,
            String filename) {

        Path path = Path.of(filename);

        if (!Files.isRegularFile(path)) {
            System.err.println(
                    "Input file not found: " + filename);
            return;
        }

        InputStream originalInput = System.in;

        try (InputStream fileInput =
                Files.newInputStream(path)) {

            System.setIn(fileInput);

            System.out.println(
                    "\nReading CLI input from: "
                    + filename);

            launchCLI(controller);

        } catch (IOException e) {

            System.err.println(
                    "Unable to read input file: "
                    + e.getMessage());

        } finally {

            System.setIn(originalInput);
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

    private static void printHelp() {

        System.out.println("\nTripGoAI options:");

        System.out.println(
                "  (no options)       Choose GUI or CLI");

        System.out.println(
                "  -cli               Start CLI directly");

        System.out.println(
                "  -input task.txt    Read CLI input from file");

        System.out.println(
                "  -LLMAPI            Start CLI (alternative flag)");

        System.out.println(
                "  -api               Start local HTTP API");

        System.out.println(
                "  -port 8080         Select API port");

        System.out.println(
                "  -help              Display available options");

        System.out.println("\nExamples:");

        System.out.println(
                "  java -jar TripGoAI.jar -cli");

        System.out.println(
                "  java -jar TripGoAI.jar -cli -input task.txt");

        System.out.println(
                "  java -jar TripGoAI.jar -api");

        System.out.println(
                "  java -jar TripGoAI.jar -api -port 8080");
    }
}
