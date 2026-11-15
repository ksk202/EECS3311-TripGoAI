
package com.tripgoai;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TripRepository {

    private static final String FILE_NAME = "trips.dat";

    private Map<Integer, Trip> trips = new HashMap<>();

    public TripRepository() {
        loadTrips();
    }

    public void saveTrip(Trip trip) {
        if (trip == null) {
            throw new IllegalArgumentException("Trip cannot be null.");
        }

        trips.put(trip.getTripId(), trip);
        saveToFile();
    }

    public Trip findTrip(int tripId) {
        return trips.get(tripId);
    }

    public boolean deleteTrip(int tripId) {
        boolean removed = trips.remove(tripId) != null;

        if (removed) {
            saveToFile();
        }

        return removed;
    }

    public int getTripCount() {
        return trips.size();
    }

    private void saveToFile() {
        try (ObjectOutputStream output =
                new ObjectOutputStream(
                        new FileOutputStream(FILE_NAME))) {

            output.writeObject(trips);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to save trips.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTrips() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream input =
                new ObjectInputStream(
                        new FileInputStream(file))) {

            Object data = input.readObject();

            if (!(data instanceof Map<?, ?>)) {
                throw new IOException("Invalid trip file format.");
            }

            trips = (Map<Integer, Trip>) data;

        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Failed to load saved trips.", e);
        }
    }
}
