/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import common.*;
import model.*;

import javax.swing.*;
import java.util.HashMap;
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

        v.assignRoute(route);
        v.setState(VehicleState.ON_ROUTE);

        return ActionResult.success("Route assigned");
    }

    // for UI ?? but need to discuss
    public void registerRoute(Route route) {
        routes.put(route.getId().toString(), route);
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
            Tile a = stops.get(i).getOccupiedTile();
            Tile b = stops.get(i + 1).getOccupiedTile();

            if (!world.getRoadNetwork().isConnected(a, b)) {
                return false;
            }
        }
        return true;

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
