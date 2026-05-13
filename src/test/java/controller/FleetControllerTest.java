package controller;

import common.GridPos;
import common.Id;
import common.Money;
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
    // happy path: player can buy one small truck from garage
    void buyTruckShouldSucceedForKnownSmallSpec() {
        // step 1: create one dummy garage on map for vehicle purchase
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);

        Money cashBefore = company.getEconomy().getCash();

        // step 2: buy one small truck
        ActionResult result = fleet.buyTruck(garage, "small");

        // step 3: company should own one vehicle and cash should go down
        assertTrue(result.isSuccess());
        assertEquals(1, company.getFleet().size());
        assertTrue(company.getEconomy().getCash().lessThan(cashBefore));
    }

    @Test
    // error path: unknown truck spec should be rejected
    void buyTruckShouldFailForUnknownSpec() {
        // step 1: create one garage and use invalid truck spec name
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);

        // step 2: try to buy unknown truck spec
        ActionResult result = fleet.buyTruck(garage, "spaceship");

        // step 3: build should fail and fleet size should stay zero
        assertFalse(result.isSuccess());
        assertEquals("Unknown truck spec: spaceship", result.getMessage());
        assertTrue(company.getFleet().isEmpty());
    }

    @Test
    // happy path: bought vehicle can be sold back and company gets resale money
    void sellVehicleShouldSucceedAfterBuyingVehicle() {
        // step 1: buy one vehicle first from dummy garage
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);

        assertTrue(fleet.buyTruck(garage, "small").isSuccess());
        Vehicle vehicle = company.getFleet().get(0);
        Money cashAfterPurchase = company.getEconomy().getCash();

        // step 2: sell the bought vehicle by id
        ActionResult result = fleet.sellVehicle(vehicle.getId().toString());

        // step 3: fleet should go back to zero and company should earn resale money
        assertTrue(result.isSuccess());
        assertEquals(0, company.getFleet().size());
        assertEquals(
                cashAfterPurchase.add(Company.getVehicleResaleValue()),
                company.getEconomy().getCash()
        );
    }

    @Test
    void createRouteDoesNotAutoPurchaseOrCreateVehicle() {
        // step 1: create one valid route and one reachable garage
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Route route = buildSimpleRoute(world);
        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);

        ActionResult result = fleet.createRoute(route.getStops());

        // step 2: route creation should not auto-buy or auto-create company vehicles
        assertTrue(result.isSuccess());
        assertEquals("Route created with 2 stops", result.getMessage());
        assertTrue(company.getFleet().isEmpty());
    }

    @Test
    void createRouteAssignsItToReachableGarage() {
        // step 1: create one valid route and one reachable garage
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Route route = buildSimpleRoute(world);

        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);

        ActionResult result = fleet.createRoute(route.getStops());

        // step 2: created route should be assigned to that garage
        assertTrue(result.isSuccess());
        assertNotNull(garage.getRoute());
        assertEquals(2, garage.getRoute().getStopCount());
    }

    @Test
    void purchasedVehicleStartsRouteFromGarage() {
        // step 1: create route first, then buy one garage vehicle
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Route route = buildSimpleRoute(world);

        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);
        assertTrue(fleet.createRoute(route.getStops()).isSuccess());

        Vehicle vehicle = garage.getVehicles().get(0);
        ActionResult purchaseResult = fleet.purchaseVehicleInGarage(garage, vehicle);

        // step 2: purchased vehicle should now belong to route-garage setup
        assertTrue(purchaseResult.isSuccess());
        assertNotNull(garage.getRoute());
        assertEquals(2, garage.getRoute().getStopCount());

        // step 3: after ticking, vehicle should start from garage and move out
        vehicle.tick(0.1);

        assertEquals(garageTile.getPos(), vehicle.getTilePos());
        assertNotNull(vehicle.getCurrentPathTile());
        assertTrue(vehicle.getState() == VehicleState.ON_ROUTE || vehicle.getState() == VehicleState.LOADING);

        vehicle.tick(1.0);
        boolean stillCenteredInGarage = garageTile.getPos().equals(vehicle.getTilePos())
                && vehicle.getSegmentProgress() <= 1e-9;
        assertFalse(stillCenteredInGarage);
    }

    @Test
    void vehicleBoughtBeforeRouteStartsAfterRouteCreation() {
        // step 1: buy one garage vehicle before any route exists
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);

        prepareOpenTile(world, new GridPos(4, 4));
        Tile garageTile = world.getMap().getTile(new GridPos(4, 4));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);

        Vehicle vehicle = garage.getVehicles().get(0);
        ActionResult purchaseResult = fleet.purchaseVehicleInGarage(garage, vehicle);
        assertTrue(purchaseResult.isSuccess());
        assertFalse(vehicle.hasRoute());

        Route route = buildSimpleRoute(world);
        ActionResult routeResult = fleet.createRoute(route.getStops());
        assertTrue(routeResult.isSuccess());

        // step 2: after route creation, already-owned idle vehicle should start using that route
        vehicle.tick(0.1);

        assertNotNull(garage.getRoute());
        assertTrue(vehicle.hasRoute());
        assertEquals(garageTile.getPos(), vehicle.getTilePos());
        assertTrue(vehicle.getState() == VehicleState.ON_ROUTE || vehicle.getState() == VehicleState.LOADING);

        vehicle.tick(1.0);
        boolean stillCenteredInGarage = garageTile.getPos().equals(vehicle.getTilePos())
                && vehicle.getSegmentProgress() <= 1e-9;
        assertFalse(stillCenteredInGarage);
    }

    @Test
    void assignRouteFailsWhenGarageCannotReachRouteStart() {
        // step 1: create one valid route but place garage on isolated area
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

        // step 2: route assignment should fail because garage cannot reach route start
        assertFalse(result.isSuccess());
        assertEquals("Vehicle garage is not connected to the route start", result.getMessage());
    }

    @Test
    void createRouteFailsWithoutReachableGarage() {
        // step 1: create valid route but do not create any reachable garage
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Route route = buildSimpleRoute(world);

        ActionResult result = fleet.createRoute(route.getStops());

        // step 2: route creation should fail without available garage
        assertFalse(result.isSuccess());
        assertEquals("Build a garage with free space before creating a route", result.getMessage());
    }

    @Test
    void vehicleAutomaticallyRestartsAfterMaintenance() {
        // step 1: create route, buy vehicle, and let time pass until maintenance is finished
        World world = new World(25, 25);
        Company company = new Company();
        FleetController fleet = new FleetController(company, world);
        Route route = buildSimpleRoute(world);

        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);
        assertTrue(fleet.createRoute(route.getStops()).isSuccess());

        Vehicle vehicle = garage.getVehicles().get(0);
        assertTrue(fleet.purchaseVehicleInGarage(garage, vehicle).isSuccess());

        for (int i = 0; i < 320; i++) {
            company.tick(1.0);
            // wait until vehicle finishes maintenance and auto-resumes
            if (vehicle.getState() == VehicleState.ON_ROUTE && vehicle.hasRoute() && garageTile.getPos().equals(vehicle.getTilePos())) {
                break;
            }
        }

        assertEquals(VehicleState.ON_ROUTE, vehicle.getState());
        assertTrue(vehicle.hasRoute());
        
        ActionResult resumeResult = fleet.resumeVehicle(vehicle.getId().toString());
        assertFalse(resumeResult.isSuccess());
    }

    private Route buildSimpleRoute(World world) {
        // helper for building one simple two-stop route on straight road
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
        // helper for making tile empty first
        Tile tile = world.getMap().getTile(pos);
        tile.setTerrain(new Land());
        tile.setEntity(null);
        tile.setRoadPiece(null);
        tile.setStop(null);
        tile.setGarage(null);
    }

    private void placeRoad(World world, GridPos pos) {
        // helper for placing one simple road tile
        Tile tile = world.getMap().getTile(pos);
        RoadPiece road = new RoadPiece(RoadKind.ROAD, null);
        road.addTile(tile);
        tile.setRoadPiece(road);
    }
}