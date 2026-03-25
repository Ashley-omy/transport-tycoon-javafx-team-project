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
 * @author Qian
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

        v.setWorld(world);
        v.assignRoute(route);
        v.setState(VehicleState.ON_ROUTE);

        return ActionResult.success("Route assigned");
    }

    // for UI ?? but need to discuss
    public void registerRoute(Route route) {
        routes.put(route.getId().toString(), route);
    }

    public ActionResult createRouteWithVehicle(List<Stop> selectedStops) {
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

        Vehicle vehicle = createVehicleFor(route);
        if (!company.buyVehicle(vehicle)) {
            return ActionResult.fail("Not enough money to create vehicle for route");
        }

        registerRoute(route);
        vehicle.setWorld(world);
        vehicle.assignRoute(route);
        vehicle.setState(VehicleState.ON_ROUTE);

        return ActionResult.success(
                "Route created with " + routeStops.size() + " stops and vehicle " + vehicle.getId()
        );
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
    //This is a temporary function used instead of Garage.
    private Vehicle createVehicleFor(Route route) {
        // Use a bus only when every stop serves a city; otherwise use a truck.
        boolean passengerRoute = route.getStops().stream()
                .allMatch(stop -> stop.getServedPlace() instanceof City);

        if (passengerRoute) {
            return VehicleFactory.createSmallBus(Id.genNew());
        }
        return VehicleFactory.createSmallTruck(Id.genNew());
    }

    // will implement during MS 3
    public ActionResult buyTruck(Garage garage, String specName) {
        return null;
    }

    public ActionResult buyBus(Garage garage, String specName) {
        return null;
    }

    public ActionResult sellVehicle(String vehicled) {
        return null;
    }
}
