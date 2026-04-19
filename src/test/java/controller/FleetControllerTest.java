package controller;

import common.GridPos;
import common.Id;
import model.City;
import model.Company;
import model.Garage;
import model.Land;
import model.RoadPiece;
import model.RoadKind;
import model.Route;
import model.Stop;
import model.Tile;
import model.Vehicle;
import model.VehicleState;
import model.World;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetControllerTest {

    @Test
    void createRouteDoesNotAutoPurchaseOrCreateVehicle() {
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Route route = buildSimpleRoute(world);

        ActionResult result = fleet.createRoute(route.getStops());

        assertTrue(result.isSuccess());
        assertEquals("Route created with 2 stops", result.getMessage());
        assertTrue(company.getFleet().isEmpty());
    }

    @Test
    void createRouteAssignsItToReachableGarage() {
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Route route = buildSimpleRoute(world);

        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);

        ActionResult result = fleet.createRoute(route.getStops());

        assertTrue(result.isSuccess());
        assertNotNull(garage.getRoute());
        assertEquals(2, garage.getRoute().getStopCount());
    }

    @Test
    void purchasedVehicleStartsRouteFromGarage() {
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Route route = buildSimpleRoute(world);

        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);
        garage.setRoute(route);

        Vehicle vehicle = garage.getVehicles().get(0);
        ActionResult purchaseResult = fleet.purchaseVehicleInGarage(garage, vehicle);

        assertTrue(purchaseResult.isSuccess());

        vehicle.tick(0.1);

        assertEquals(garageTile.getPos(), vehicle.getTilePos());
        assertNotNull(vehicle.getWorldPos());
        assertTrue(vehicle.getState() == VehicleState.ON_ROUTE || vehicle.getState() == VehicleState.LOADING);
    }

    @Test
    void assignRouteFailsWhenGarageCannotReachRouteStart() {
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Route route = buildSimpleRoute(world);

        prepareOpenTile(world, new GridPos(20, 20));
        prepareOpenTile(world, new GridPos(19, 20));
        prepareOpenTile(world, new GridPos(21, 20));
        prepareOpenTile(world, new GridPos(20, 19));
        prepareOpenTile(world, new GridPos(20, 21));
        world.getRoadNetwork().rebuild(world.getMap());

        Tile isolatedGarageTile = world.getMap().getTile(new GridPos(20, 20));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(isolatedGarageTile));
        isolatedGarageTile.setGarage(garage);

        Vehicle vehicle = garage.getVehicles().get(0);
        assertTrue(fleet.purchaseVehicleInGarage(garage, vehicle).isSuccess());

        ActionResult result = fleet.assignRoute(vehicle.getId().toString(), route);

        assertFalse(result.isSuccess());
        assertEquals("Vehicle garage is not connected to the route start", result.getMessage());
    }

    private Route buildSimpleRoute(World world) {
        prepareOpenTile(world, new GridPos(5, 4));
        prepareOpenTile(world, new GridPos(6, 4));
        prepareOpenTile(world, new GridPos(7, 4));
        prepareOpenTile(world, new GridPos(5, 5));
        prepareOpenTile(world, new GridPos(6, 5));
        prepareOpenTile(world, new GridPos(7, 5));

        placeRoad(world, new GridPos(5, 4));
        placeRoad(world, new GridPos(6, 4));
        placeRoad(world, new GridPos(7, 4));
        world.getRoadNetwork().rebuild(world.getMap());

        Stop first = new Stop(Id.genNew(), world.getMap().getTile(new GridPos(6, 5)), new City(Id.genNew()));
        Stop second = new Stop(Id.genNew(), world.getMap().getTile(new GridPos(7, 5)), new City(Id.genNew()));
        return new Route(Id.genNew(), List.of(first, second));
    }

    private void prepareOpenTile(World world, GridPos pos) {
        Tile tile = world.getMap().getTile(pos);
        tile.setTerrain(new Land());
        tile.setEntity(null);
        tile.setRoadPiece(null);
        tile.setStop(null);
        tile.setGarage(null);
    }

    private void placeRoad(World world, GridPos pos) {
        Tile tile = world.getMap().getTile(pos);
        RoadPiece road = new RoadPiece(RoadKind.ROAD, null);
        road.addTile(tile);
        tile.setRoadPiece(road);
    }
}
