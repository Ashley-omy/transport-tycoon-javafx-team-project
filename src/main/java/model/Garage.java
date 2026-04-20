/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import common.Id;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Garage {
    private static final int DEFAULT_STOCK_PER_TYPE = 2;
    private static final int DEFAULT_INITIAL_STOCK_SIZE = DEFAULT_STOCK_PER_TYPE * 2;
    private static final double DEFAULT_EVENT_DISPLAY_SECONDS = 2.0;

    private final Id id;
    private final int capacity;
    private final int serviceBayCount;

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Tile> occupiedTiles = new ArrayList<>();
    private final List<GarageEventDisplay> activeEventDisplays = new ArrayList<>();
    private Route route;

    public Garage(Id id, int capacity, int serviceBayCount, List<Tile> occupiedTiles) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        if (capacity < DEFAULT_INITIAL_STOCK_SIZE) {
            throw new IllegalArgumentException("capacity must be >= " + DEFAULT_INITIAL_STOCK_SIZE);
        }
        if (serviceBayCount <= 0) {
            throw new IllegalArgumentException("serviceBayCount must be > 0");
        }
        if (occupiedTiles == null || occupiedTiles.isEmpty()) {
            throw new IllegalArgumentException("occupiedTiles cannot be null or empty");
        }
        this.capacity = capacity;
        this.serviceBayCount = serviceBayCount;
        this.occupiedTiles.addAll(occupiedTiles);
        populateInitialStock();
    }

    public Id getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getServiceBayCount() {
        return serviceBayCount;
    }

    public List<Vehicle> getVehicles() {
        return Collections.unmodifiableList(vehicles);
    }

    public List<Tile> getOccupiedTiles() {
        return Collections.unmodifiableList(occupiedTiles);
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public boolean hasRoute() {
        return route != null;
    }

    public boolean isFull() {
        return vehicles.size() >= capacity;
    }

    public boolean addVehicle(Vehicle v) {
        if (v != null && !isFull() && !vehicles.contains(v)) {
            vehicles.add(v);
            return true;
        }
        return false;
    }

    public void removeVehicle(Vehicle v) {
        vehicles.remove(v);
    }

    public void pushEventDisplay(String text) {
        pushEventDisplay(text, DEFAULT_EVENT_DISPLAY_SECONDS);
    }

    public void pushEventDisplay(String text, double durationSeconds) {
        if (text == null || text.isBlank()) {
            return;
        }
        if (Double.isNaN(durationSeconds) || Double.isInfinite(durationSeconds) || durationSeconds <= 0.0) {
            return;
        }
        activeEventDisplays.add(new GarageEventDisplay(text, durationSeconds));
    }

    public List<String> getActiveEventDisplayTexts() {
        if (activeEventDisplays.isEmpty()) {
            return List.of();
        }
        List<String> texts = new ArrayList<>(activeEventDisplays.size());
        for (GarageEventDisplay display : activeEventDisplays) {
            texts.add(display.text);
        }
        return Collections.unmodifiableList(texts);
    }

    public void tickEventDisplays(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) {
            return;
        }
        Iterator<GarageEventDisplay> iterator = activeEventDisplays.iterator();
        while (iterator.hasNext()) {
            GarageEventDisplay display = iterator.next();
            display.remainingSeconds -= deltaTime;
            if (display.remainingSeconds <= 0.0) {
                iterator.remove();
            }
        }
    }

    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;
        tickEventDisplays(deltaTime);
    }

    // Company.buyVehicle(v) is called by FleetController
    public boolean sellVehicle(Vehicle v) {
        if (v == null) return false;
        removeVehicle(v);
        return true;
    }
    
    public int getAvailableSpace() {
        return capacity - vehicles.size();
    }
    
    public boolean hasVehicle(Vehicle v) {
        return vehicles.contains(v);
    }

    private void populateInitialStock() {
        for (int i = 0; i < DEFAULT_STOCK_PER_TYPE; i++) {
            stockVehicle(VehicleFactory.createSmallBus(Id.genNew()));
            stockVehicle(VehicleFactory.createSmallTruck(Id.genNew()));
        }
    }

    private void stockVehicle(Vehicle vehicle) {
        vehicle.setHomeGarage(this);
        vehicles.add(vehicle);
    }

    private static final class GarageEventDisplay {
        private final String text;
        private double remainingSeconds;

        private GarageEventDisplay(String text, double remainingSeconds) {
            this.text = text;
            this.remainingSeconds = remainingSeconds;
        }
    }
}
