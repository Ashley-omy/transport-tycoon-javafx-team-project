/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import common.Id;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Garage {

    private final Id id;
    private final int capacity;
    private final int serviceBayCount;

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Tile> occupiedTiles = new ArrayList<>();

    public Garage(Id id, int capacity, int serviceBayCount, List<Tile> occupiedTiles) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
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

    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;
        // Garage mainly stores vehicles; actual maintenance is handled by Company
        // Vehicles in garage are parked and not ticking
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
}
