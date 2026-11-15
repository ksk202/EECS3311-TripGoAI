
package com.tripgoai;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MemoryManager {

    // Singleton instance
    private static MemoryManager instance;

    // Store user preferences
    private final Map<String, String> preferences;

    // Persistent storage file
    private static final Path FILE_PATH = Paths.get(
            System.getProperty("user.home"),
            ".tripgoai",
            "preferences.properties"
    );

    // Private constructor for Singleton Pattern
    private MemoryManager() {
        preferences = new HashMap<>();
        loadPreferences();
    }

    // Singleton access method
    public static synchronized MemoryManager getInstance() {
        if (instance == null) {
            instance = new MemoryManager();
        }
        return instance;
    }

    // Save or update a travel preference
    public synchronized void savePreference(String key, String value) {

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Preference key cannot be empty.");
        }

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Preference value cannot be empty.");
        }

        String normalizedKey = key.trim().toLowerCase();

        preferences.put(normalizedKey, value.trim());

        savePreferencesToFile();
    }

    // Retrieve a single preference
    public synchronized String getPreference(String key) {

        if (key == null || key.isBlank()) {
            return null;
        }

        String normalizedKey = key.trim().toLowerCase();

        return preferences.get(normalizedKey);
    }

    // Retrieve all saved preferences
    public synchronized Map<String, String> getPreferences() {
        return new HashMap<>(preferences);
    }

    // Save preferences permanently
    private void savePreferencesToFile() {

        Properties properties = new Properties();

        properties.putAll(preferences);

        try {
            Files.createDirectories(FILE_PATH.getParent());

            try (OutputStream output =
                    Files.newOutputStream(FILE_PATH)) {

                properties.store(
                        output,
                        "TripGoAI Saved Travel Preferences"
                );
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to save travel preferences: "
                    + e.getMessage(),
                    e
            );
        }
    }

    // Load preferences when application starts
    private void loadPreferences() {

        if (!Files.exists(FILE_PATH)) {
            return;
        }

        Properties properties = new Properties();

        try (InputStream input =
                Files.newInputStream(FILE_PATH)) {

            properties.load(input);

            for (String key : properties.stringPropertyNames()) {

                String normalizedKey =
                        key.trim().toLowerCase();

                preferences.put(
                        normalizedKey,
                        properties.getProperty(key)
                );
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load travel preferences: "
                    + e.getMessage(),
                    e
            );
        }
    }
}
