import java.util.*;
import java.time.*;

public class ParkingLot {
    private static final int TOTAL_SPOTS = 500;
    private ParkingSpot[] spots;
    private int totalOccupied;
    private Map<String, LocalDateTime> entryTimes;
    private Map<String, LocalDateTime> exitTimes;

    // Constructor to initialize parking spots
    public ParkingLot() {
        spots = new ParkingSpot[TOTAL_SPOTS];
        entryTimes = new HashMap<>();
        exitTimes = new HashMap<>();
        totalOccupied = 0;
    }

    // Hash function to map vehicle license plate to a parking spot
    private int hash(String licensePlate) {
        return licensePlate.hashCode() % TOTAL_SPOTS;
    }

    // Park a vehicle based on license plate
    public boolean parkVehicle(String licensePlate) {
        int spot = hash(licensePlate);

        // Try to park the vehicle using linear probing
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            int currentSpot = (spot + i) % TOTAL_SPOTS;

            // If spot is empty, park the vehicle
            if (spots[currentSpot] == null) {
                spots[currentSpot] = new ParkingSpot(licensePlate);
                entryTimes.put(licensePlate, LocalDateTime.now());
                totalOccupied++;
                return true;
            }
        }
        return false;  // No spot available
    }

    // Remove a vehicle when it exits
    public boolean removeVehicle(String licensePlate) {
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            int spot = (hash(licensePlate) + i) % TOTAL_SPOTS;

            if (spots[spot] != null && spots[spot].licensePlate.equals(licensePlate)) {
                exitTimes.put(licensePlate, LocalDateTime.now());
                spots[spot] = null;
                totalOccupied--;
                return true;
            }
        }
        return false;  // Vehicle not found
    }

    // Find the nearest available spot
    public int findNearestSpot() {
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            if (spots[i] == null) {
                return i;
            }
        }
        return -1;  // No spot available
    }

    // Calculate parking statistics (average occupancy)
    public double averageOccupancy() {
        return (double) totalOccupied / TOTAL_SPOTS * 100;
    }

    // Calculate peak hours (based on entry/exit times)
    public Map<Integer, Integer> calculatePeakHours() {
        Map<Integer, Integer> hourlyOccupancy = new HashMap<>();

        for (String licensePlate : entryTimes.keySet()) {
            LocalDateTime entry = entryTimes.get(licensePlate);
            int hour = entry.getHour();
            hourlyOccupancy.put(hour, hourlyOccupancy.getOrDefault(hour, 0) + 1);
        }

        return hourlyOccupancy;
    }

    // Parking Spot class
    private static class ParkingSpot {
        String licensePlate;

        ParkingSpot(String licensePlate) {
            this.licensePlate = licensePlate;
        }
    }

    // Main method for testing the parking lot system
    public static void main(String[] args) {
        ParkingLot lot = new ParkingLot();

        // Test park vehicles
        System.out.println(lot.parkVehicle("ABC123")); // true
        System.out.println(lot.parkVehicle("XYZ789")); // true

        // Test removing vehicles
        System.out.println(lot.removeVehicle("ABC123")); // true
        System.out.println(lot.removeVehicle("XYZ789")); // true

        // Test statistics
        System.out.println("Average occupancy: " + lot.averageOccupancy() + "%");

        Map<Integer, Integer> peakHours = lot.calculatePeakHours();
        System.out.println("Peak hours (hourly): " + peakHours);
    }
}