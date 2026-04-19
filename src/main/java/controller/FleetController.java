/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import common.*;
import model.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Qian, Asuna
 */

// where we put path pick?? UI or route or controller???

public class FleetController {
    private final Company company;
    private final World world;

    // not sure how to deal with routeId
    // for now i store here
    private final Map<String, Route> routes = new HashMap<>();

    public FleetController(Company company, World world) {
        if (world == null) throw new IllegalArgumentException("world cannot be null");
        if (company == null) throw new IllegalArgumentException("company cannot be null");
        this.company = company;
        this.world = world;
    }

    public ActionResult assignRoute(String vehicleId, Route route) {
        // step 1: do we have such vehicle in company? -> company is our assets($ + vehicles)
        Vehicle v = findVehicleInCompany(vehicleId);
        if (v == null) return ActionResult.fail("Vehicle not found");

        // step2: find if the route legal(route has 2 stops -> stops are connected in roadNetwork??)
        if (!isRouteValid(route)) return ActionResult.fail("Route is not connected");
        if (v.getHomeGarage() == null || v.getHomeGarage().getOccupiedTiles().isEmpty()) {
            return ActionResult.fail("Vehicle must have a home garage before route assignment");
        }
        if (!canReachRouteFromGarage(v, route)) {
            return ActionResult.fail("Vehicle garage is not connected to the route start");
        }

        v.setWorld(world);
        v.assignRoute(route);
        v.setState(VehicleState.ON_ROUTE);

        return ActionResult.success("Route assigned");
    }

    // for UI ?? but need to discuss
    public void registerRoute(Route route) {
        routes.put(route.getId().toString(), route);
    }

    public ActionResult createRoute(List<Stop> selectedStops) {
        if (selectedStops == null || selectedStops.size() < 2) {
            return ActionResult.fail("Select at least two stops");
        }

        // Deduplicate the UI selection before creating the route instance.
        List<Stop> routeStops = new ArrayList<>();
        for (Stop stop : selectedStops) {
            if (stop == null) {
                return ActionResult.fail("Selected stops contain an invalid stop");
            }
            if (routeStops.contains(stop)) {
                continue;
            }
            routeStops.add(stop);
        }

        if (routeStops.size() < 2) {
            return ActionResult.fail("Select at least two different stops");
        }

        Route route = new Route(Id.genNew(), routeStops);
        if (!isRouteValid(route)) {
            return ActionResult.fail("Selected stops are not connected by road");
        }

        registerRoute(route);
        Garage garage = findReachableGarageForRoute(route);
        if (garage != null) {
            garage.setRoute(route);
        }
        return ActionResult.success("Route created with " + routeStops.size() + " stops");
    }

    // for UI
    public ActionResult assignRoute(String vehicleId, String routeId) {
        Route r = routes.get(routeId);
        if (r == null) {
            return ActionResult.fail("Route not found");
        }
        return assignRoute(vehicleId, r);
    }

    // helper fns for checking if route is legal
    // vehicle in company??
    private Vehicle findVehicleInCompany(String id) {
        for (Vehicle v : company.getFleet()) {
            if (v.getId().toString().equals(id)) {
                return v;
            }
        }
        return null;
    }
    // do we have connected road between stops??
    private boolean isRouteValid(Route route) {
        List<Stop> stops = route.getStops();

        for (int i = 0; i < stops.size()-1; ++i) {
            if (!areStopsConnected(stops.get(i), stops.get(i + 1))) {
                return false;
            }
        }
        return true;

    }

    private boolean areStopsConnected(Stop firstStop, Stop secondStop) {
        // Route validation must work through the roads next to each stop.
        List<Tile> firstRoadTiles = getAdjacentRoadTiles(firstStop);
        List<Tile> secondRoadTiles = getAdjacentRoadTiles(secondStop);

        for (Tile firstRoadTile : firstRoadTiles) {
            for (Tile secondRoadTile : secondRoadTiles) {
                if (world.getRoadNetwork().isConnected(firstRoadTile, secondRoadTile)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Tile> getAdjacentRoadTiles(Stop stop) {
        // Stops themselves are not road tiles, so inspect their 4-neighbors instead.
        List<Tile> roadTiles = new ArrayList<>();
        GridPos pos = stop.getOccupiedTile().getPos();

        for (GridPos neighborPos : List.of(
                pos.add(1, 0),
                pos.add(-1, 0),
                pos.add(0, 1),
                pos.add(0, -1)
        )) {
            if (!world.getMap().inBounds(neighborPos)) {
                continue;
            }

            Tile neighbor = world.getMap().getTile(neighborPos);
            if (neighbor.getRoadPiece() != null) {
                roadTiles.add(neighbor);
            }
        }

        return roadTiles;
    }
    private boolean purchaseVehicleInGarageInternal(Vehicle vehicle, Garage garage) {
        if (vehicle == null || garage == null) {
            return false;
        }

        // New stock vehicles need free garage capacity. Pre-stocked vehicles can be bought regardless.
        if (!garage.hasVehicle(vehicle) && garage.isFull()) {
            return false;
        }

        if (vehicle.getHomeGarage() != null && vehicle.getHomeGarage() != garage) {
            return false;
        }
        if (vehicle.getHomeGarage() == null) {
            vehicle.setHomeGarage(garage);
        }
        vehicle.setWorld(world);

        if (!company.buyVehicle(vehicle)) {
            return false;
        }

        // Keep purchased vehicles visible in the garage list (as OWNED in GaragePane).
        if (!garage.hasVehicle(vehicle)) {
            if (!garage.addVehicle(vehicle)) {
                return false;
            }
        }
        autoAssignGarageRoute(vehicle, garage);
        return true;
    }

    public ActionResult purchaseVehicleInGarage(Garage garage, Vehicle vehicle) {
        if (garage == null) return ActionResult.fail("Garage cannot be null");
        if (vehicle == null) return ActionResult.fail("Vehicle cannot be null");
        if (!garage.hasVehicle(vehicle)) {
            return ActionResult.fail("Selected vehicle is not available in this garage");
        }
        if (company.getFleet().contains(vehicle)) {
            return ActionResult.fail("Selected vehicle is already owned");
        }

        if (!purchaseVehicleInGarageInternal(vehicle, garage)) {
            return ActionResult.fail("Not enough money to buy vehicle");
        }
        return ActionResult.success("Vehicle purchased: " + vehicle.getId());
    }

    public ActionResult buyTruck(Garage garage, String specName) {
        if (garage == null) return ActionResult.fail("Garage cannot be null");
        if (garage.isFull()) return ActionResult.fail("Garage is full");
        
        Vehicle truck;
        if ("small".equalsIgnoreCase(specName)) {
            truck = VehicleFactory.createSmallTruck(Id.genNew());
        } else if ("large".equalsIgnoreCase(specName)) {
            truck = VehicleFactory.createLargeTruck(Id.genNew());
        } else {
            return ActionResult.fail("Unknown truck spec: " + specName);
        }

        if (!purchaseVehicleInGarageInternal(truck, garage)) {
            return ActionResult.fail("Not enough money to buy truck");
        }
        return ActionResult.success("Truck purchased: " + truck.getId());
    }

    public ActionResult buyBus(Garage garage, String specName) {
        if (garage == null) return ActionResult.fail("Garage cannot be null");
        if (garage.isFull()) return ActionResult.fail("Garage is full");
        
        Vehicle bus;
        if ("small".equalsIgnoreCase(specName)) {
            bus = VehicleFactory.createSmallBus(Id.genNew());
        } else if ("large".equalsIgnoreCase(specName)) {
            bus = VehicleFactory.createLargeBus(Id.genNew());
        } else {
            return ActionResult.fail("Unknown bus spec: " + specName);
        }

        if (!purchaseVehicleInGarageInternal(bus, garage)) {
            return ActionResult.fail("Not enough money to buy bus");
        }
        return ActionResult.success("Bus purchased: " + bus.getId());
    }

    public ActionResult sellVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.isEmpty()) {
            return ActionResult.fail("Vehicle ID cannot be null or empty");
        }
        
        Vehicle vehicle = findVehicleInCompany(vehicleId);
        if (vehicle == null) {
            return ActionResult.fail("Vehicle not found");
        }

        // Keep vehicle in garage stock list so it can appear as on-sale in the garage pane.
        company.sellVehicle(vehicle);
        return ActionResult.success("Vehicle sold: " + vehicleId);
    }

    public ActionResult sellOverAgedVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.isEmpty()) {
            return ActionResult.fail("Vehicle ID cannot be null or empty");
        }

        Vehicle vehicle = findVehicleInCompany(vehicleId);
        if (vehicle == null) {
            return ActionResult.fail("Vehicle not found");
        }

        if (!vehicle.isOverAged()) {
            return ActionResult.fail("Vehicle is not over-aged yet");
        }

        return sellVehicle(vehicleId);
    }

    public ActionResult sellAllOverAgedVehicles() {
        List<Vehicle> overAgedVehicles = new ArrayList<>();
        for (Vehicle vehicle : company.getFleet()) {
            if (vehicle.isOverAged()) {
                overAgedVehicles.add(vehicle);
            }
        }

        if (overAgedVehicles.isEmpty()) {
            return ActionResult.fail("No over-aged vehicles available for sale");
        }

        for (Vehicle vehicle : overAgedVehicles) {
            company.sellVehicle(vehicle);
        }

        return ActionResult.success("Sold " + overAgedVehicles.size() + " over-aged vehicle(s)");
    }

    private boolean canReachRouteFromGarage(Vehicle vehicle, Route route) {
        GridPos garagePos = vehicle.getHomeGarage().getOccupiedTiles().get(0).getPos();
        GridPos firstStopPos = route.getStop(0).getOccupiedTile().getPos();
        return !world.getRoadNetwork().findPathBetweenLocations(world.getMap(), garagePos, firstStopPos).isEmpty();
    }

    private Garage findReachableGarageForRoute(Route route) {
        if (route == null) {
            return null;
        }

        GameMap map = world.getMap();
        for (Tile[] column : map.getTiles()) {
            for (Tile tile : column) {
                Garage garage = tile.getGarage();
                if (garage == null || garage.getOccupiedTiles().isEmpty()) {
                    continue;
                }
                GridPos garagePos = garage.getOccupiedTiles().get(0).getPos();
                GridPos firstStopPos = route.getStop(0).getOccupiedTile().getPos();
                if (!world.getRoadNetwork().findPathBetweenLocations(map, garagePos, firstStopPos).isEmpty()) {
                    return garage;
                }
            }
        }
        return null;
    }

    private void autoAssignGarageRoute(Vehicle vehicle, Garage garage) {
        if (vehicle == null || garage == null || garage.getRoute() == null) {
            return;
        }
        if (!canReachRouteFromGarage(vehicle, garage.getRoute())) {
            return;
        }

        vehicle.setWorld(world);
        vehicle.assignRoute(garage.getRoute());
        vehicle.setState(VehicleState.ON_ROUTE);
    }
}
