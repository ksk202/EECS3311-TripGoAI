
package com.tripgoai;

public class DatabaseManager {

    // Singleton instance
    private static DatabaseManager instance;

    // Private constructor prevents creating objects directly
    private DatabaseManager() {
        System.out.println("DatabaseManager initialized.");
    }

    // Returns the single DatabaseManager instance
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Connect to the database
    public void connect() {
        System.out.println("Connecting to TripGoAI database...");
    }

    // Disconnect from the database
    public void disconnect() {
        System.out.println("Disconnecting from TripGoAI database...");
    }

    // Save trip information
    public void saveTrip(String tripName) {
        System.out.println("Saving trip: " + tripName);
    }

    // Delete trip information
    public void deleteTrip(String tripName) {
        System.out.println("Deleting trip: " + tripName);
    }
}