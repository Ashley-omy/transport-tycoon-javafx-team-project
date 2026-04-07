/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import common.Id;
import common.Money;
import java.util.ArrayList;
import java.util.List;

public class Garage {

    private final Id id;
    private final int capacity;
    private final int serviceBayCount;

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Tile> occupiedTiles = new ArrayList<>();

    public Garage(Id id, int capacity, int serviceBayCount, List<Tile> occupiedTiles) {
        this.id = id;
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
        return vehicles;
    }

    public List<Tile> getOccupiedTiles() {
        return occupiedTiles;
    }

    public boolean isFull() {
        return vehicles.size() >= capacity;
    }

    public boolean addVehicle(Vehicle v) {
        if (v != null && !isFull()) {
            vehicles.add(v);
            return true;
        }
        return false;
    }

    public void removeVehicle(Vehicle v) {
        vehicles.remove(v);
    }

    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || deltaTime <= 0.0) return;
        // Garage mainly stores vehicles; actual maintenance is handled by Company
        // Vehicles in garage are parked and not ticking
    }

    public void sellVehicle(Vehicle v) {
        removeVehicle(v);
        // Company.sellVehicle(v) is called by FleetController
    }
    
    public int getAvailableSpace() {
        return capacity - vehicles.size();
    }
    
    public boolean hasVehicle(Vehicle v) {
        return vehicles.contains(v);
    }
}
