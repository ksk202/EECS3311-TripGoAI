
package com.tripgoai;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class ApiServer {

    private static final int MAX_TASK_BYTES = 8192;

    private ApiServer() {
        // Utility class
    }

    public static void start(
            TravelAgentController controller,
            int port) throws IOException {

        if (controller == null) {
            throw new IllegalArgumentException(
                    "Controller cannot be null.");
        }

        // Local computer only. Not exposed to the network.
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", port),
                0);

        // Health-check endpoint
        server.createContext("/health", exchange -> {
            try {
                if (!exchange.getRequestMethod()
                        .equalsIgnoreCase("GET")) {

                    sendText(exchange, 405,
                            "Use GET for /health.");
                    return;
                }

                sendText(exchange, 200, "TripGoAI API running");

            } finally {
                exchange.close();
            }
        });

        // API input endpoint
        server.createContext("/task", exchange -> {
            try {

                if (!exchange.getRequestMethod()
                        .equalsIgnoreCase("POST")) {

                    sendText(exchange, 405,
                            "Use POST for /task.");
                    return;
                }

                String contentType =
                        exchange.getRequestHeaders()
                                .getFirst("Content-Type");

                if (contentType != null
                        && !contentType.toLowerCase()
                                .startsWith("text/plain")) {

                    sendText(exchange, 415,
                            "Use Content-Type: text/plain.");
                    return;
                }

                byte[] input = exchange.getRequestBody()
                        .readNBytes(MAX_TASK_BYTES + 1);

                if (input.length > MAX_TASK_BYTES) {
                    sendText(exchange, 413,
                            "Task is too large.");
                    return;
                }

                String task = new String(
                        input, StandardCharsets.UTF_8).trim();

                if (task.isEmpty()) {
                    sendText(exchange, 400,
                            "Task cannot be empty.");
                    return;
                }

                // Reuse the existing TripGoAI AI functionality.
                String response = controller.askAI(task);

                sendText(exchange, 200,
                        response == null ? "" : response);

            } catch (RuntimeException e) {

                System.err.println(
                        "API task failed: " + e.getMessage());

                sendText(exchange, 500,
                        "Unable to complete AI request.");

            } finally {
                exchange.close();
            }
        });

        // Process API requests one at a time.
        server.setExecutor(
                Executors.newSingleThreadExecutor());

        server.start();

        System.out.println(
                "\nTripGoAI local API started.");

        System.out.println(
                "Health check: http://127.0.0.1:"
                + port + "/health");

        System.out.println(
                "Task endpoint: http://127.0.0.1:"
                + port + "/task");

        System.out.println(
                "\nPress Ctrl+C to stop the API server.");
    }

    private static void sendText(
            HttpExchange exchange,
            int status,
            String response) throws IOException {

        byte[] bytes = response.getBytes(
                StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/plain; charset=utf-8");

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output =
                exchange.getResponseBody()) {

            output.write(bytes);
        }
    }
}
