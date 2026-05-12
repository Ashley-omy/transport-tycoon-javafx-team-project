/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import common.*;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author Qian, Asuna
 */

// where we put path pick?? UI or route or controller???

public class FleetController {
    private final Company company;
    private final World world;

    // Store created routes by id so UI can reference and assign them later.
    private final Map<String, Route> routes = new HashMap<>();

    public FleetController(Company company, World world) {
        if (world == null) throw new IllegalArgumentException("world cannot be null");
        if (company == null) throw new IllegalArgumentException("company cannot be null");
        this.company = company;
        this.world = world;
    }

    //--------- route assignment rules ----------------

    public ActionResult assignRoute(String vehicleId, Route route) {
        // step 1: do we have such vehicle in company? -> company is our assets($ + vehicles)
        Vehicle v = findVehicleInCompany(vehicleId);
        if (v == null) return ActionResult.fail("Vehicle not found");

        // step 2: route must be legal and vehicle must have a usable home garage
        if (!isRouteValid(route)) return ActionResult.fail("Route is not connected");
        if (v.getHomeGarage() == null || v.getHomeGarage().getOccupiedTiles().isEmpty()) {
            return ActionResult.fail("Vehicle must have a home garage before route assignment");
        }

        // step 3: garage must be connected to the first stop of the route
        if (!canReachRouteFromGarage(v, route)) {
            return ActionResult.fail("Vehicle garage is not connected to the route start");
        }

        // step 4: route can now be assigned and vehicle starts operating
        v.setWorld(world);
        v.assignRoute(route);
        v.setState(VehicleState.ON_ROUTE);

        return ActionResult.success("Route assigned");
    }

    // for UI ?? but need to discuss
    public void registerRoute(Route route) {
        routes.put(route.getId().toString(), route);
    }

    //--------- route creation rules ----------------

    public ActionResult createRoute(List<Stop> selectedStops) {
        // step 1: route needs at least two selected stops from UI
        if (selectedStops == null || selectedStops.size() < 2) {
            return ActionResult.fail("Select at least two stops");
        }

        // step 2: deduplicate the UI selection before creating the route instance
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

        // step 3: create route object and validate stop-to-stop connectivity
        Route route = new Route(Id.genNew(), routeStops);
        if (!isRouteValid(route)) {
            return ActionResult.fail("Selected stops are not connected by road");
        }

        // step 4: a free garage must exist so vehicles can be attached to this route
        Garage garage = findAvailableGarageForRoute(route);
        if (garage == null) {
            return ActionResult.fail("Build a garage with free space before creating a route");
        }

        // step 5: register route, bind it to garage, and start matching owned vehicles
        registerRoute(route);
        garage.setRoute(route);
        startOwnedVehiclesForGarageRoute(garage, route);
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

    public ActionResult resumeVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.isEmpty()) {
            return ActionResult.fail("Vehicle ID cannot be null or empty");
        }

        Vehicle vehicle = findVehicleInCompany(vehicleId);
        if (vehicle == null) {
            return ActionResult.fail("Vehicle not found");
        }
        if (!vehicle.hasRoute()) {
            return ActionResult.fail("Vehicle has no assigned route");
        }
        if (vehicle.getState() != VehicleState.IDLE) {
            return ActionResult.fail("Vehicle is not ready to resume");
        }
        if (vehicle.getHomeGarage() == null || vehicle.getHomeGarage().getOccupiedTiles().isEmpty()) {
            return ActionResult.fail("Vehicle must have a home garage before resuming");
        }
        if (!canReachRouteFromGarage(vehicle, vehicle.getAssignedRoute())) {
            return ActionResult.fail("Vehicle garage is not connected to the route start");
        }

        vehicle.setWorld(world);
        vehicle.setState(VehicleState.ON_ROUTE);
        return ActionResult.success("Vehicle resumed: " + vehicle.getDisplayName());
    }

    // helper fns for checking if route is legal
    private Vehicle findVehicleInCompany(String id) {
        for (Vehicle v : company.getFleet()) {
            if (v.getId().toString().equals(id)) {
                return v;
            }
        }
        return null;
    }

    // Consecutive stops in one route must be connected by the road network.
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

    //--------- vehicle purchase rules ----------------

    private boolean purchaseVehicleInGarageInternal(Vehicle vehicle, Garage garage) {
        if (vehicle == null || garage == null) {
            return false;
        }

        // New stock vehicles need free garage capacity. Pre-stocked vehicles can be bought regardless.
        if (garage.doesNotHaveVehicle(vehicle) && garage.isFull()) {
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
        if (garage.doesNotHaveVehicle(vehicle)) {
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
        if (garage.doesNotHaveVehicle(vehicle)) {
            return ActionResult.fail("Selected vehicle is not available in this garage");
        }
        if (company.getFleet().contains(vehicle)) {
            return ActionResult.fail("Selected vehicle is already owned");
        }
        if (!purchaseVehicleInGarageInternal(vehicle, garage)) {
            return ActionResult.fail("Not enough money to buy vehicle");
        }
        return ActionResult.success("Vehicle purchased: " + vehicle.getDisplayName());
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
        return ActionResult.success("Truck purchased: " + truck.getDisplayName());
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
        return ActionResult.success("Bus purchased: " + bus.getDisplayName());
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
        return ActionResult.success("Vehicle sold: " + vehicle.getDisplayName());
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

    private Garage findAvailableGarageForRoute(Route route) {
        if (route == null) {
            return null;
        }

        GameMap map = world.getMap();
        Garage bestGarage = null;
        int bestPathLength = Integer.MAX_VALUE;
        // Prefer the nearest free garage that can reach the route start.
        for (Tile[] column : map.getTiles()) {
            for (Tile tile : column) {
                Garage garage = tile.getGarage();
                if (garage == null || garage.getOccupiedTiles().isEmpty() || garage.hasRoute()) {
                    continue;
                }
                GridPos garagePos = garage.getOccupiedTiles().get(0).getPos();
                GridPos firstStopPos = route.getStop(0).getOccupiedTile().getPos();
                List<GridPos> path = world.getRoadNetwork().findPathBetweenLocations(map, garagePos, firstStopPos);
                if (!path.isEmpty() && path.size() < bestPathLength) {
                    bestPathLength = path.size();
                    bestGarage = garage;
                }
            }
        }
        return bestGarage;
    }

    // If a garage already owns a route, newly purchased idle vehicles can start it automatically.
    private boolean autoAssignGarageRoute(Vehicle vehicle, Garage garage) {
        if (vehicle == null || garage == null) {
            return false;
        }

        if (vehicle.getState() != VehicleState.IDLE) {
            return false;
        }

        Route route = garage.getRoute();
        if (route == null || !canReachRouteFromGarage(vehicle, route)) {
            return false;
        }

        vehicle.setWorld(world);
        vehicle.assignRoute(route);
        vehicle.setState(VehicleState.ON_ROUTE);
        return true;
    }

    // When a new route is created for a garage, start all compatible idle owned vehicles there.
    private void startOwnedVehiclesForGarageRoute(Garage garage, Route route) {
        if (garage == null || route == null) {
            return;
        }

        for (Vehicle vehicle : company.getFleet()) {
            if (vehicle.getHomeGarage() != garage) {
                continue;
            }
            if (vehicle.getState() != VehicleState.IDLE) {
                continue;
            }
            if (vehicle.hasRoute() && vehicle.getAssignedRoute() != route) {
                continue;
            }

            vehicle.setWorld(world);
            vehicle.assignRoute(route);
            vehicle.setState(VehicleState.ON_ROUTE);
        }
    }
}
