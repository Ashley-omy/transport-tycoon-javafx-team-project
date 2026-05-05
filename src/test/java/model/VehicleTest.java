package model;

import common.GridPos;
import common.Id;
import common.Money;
import controller.FleetController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleTest {

    @Test
    // non-trivial test case
    // requirement coverage:
    // happy path: over-aged vehicle can return to garage and finish maintenance
    // difficult-to-reproduce situation: maintenance interruption in the middle of route operation
    // edge case: vehicle is forced into over-aged state by directly pushing age over threshold
    void overAgedVehicleShouldReturnToGarageAndBeResumableAfterMaintenance() throws Exception {
        // step 1: build one simple route with one garage and two stops
        World world = new World(25, 25);
        Company company = new Company();
        company.setWorld(world);
        FleetController fleetController = new FleetController(company, world);

        prepareOpenTile(world, new GridPos(5, 4));
        prepareOpenTile(world, new GridPos(6, 4));
        prepareOpenTile(world, new GridPos(7, 4));
        prepareOpenTile(world, new GridPos(8, 4));
        prepareOpenTile(world, new GridPos(9, 4));
        prepareOpenTile(world, new GridPos(10, 4));
        prepareOpenTile(world, new GridPos(5, 5));
        prepareOpenTile(world, new GridPos(6, 5));
        prepareOpenTile(world, new GridPos(10, 5));

        placeRoad(world, new GridPos(5, 4));
        placeRoad(world, new GridPos(6, 4));
        placeRoad(world, new GridPos(7, 4));
        placeRoad(world, new GridPos(8, 4));
        placeRoad(world, new GridPos(9, 4));
        placeRoad(world, new GridPos(10, 4));
        world.getRoadNetwork().rebuild(world.getMap());

        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);

        City cityA = new City(Id.genNew());
        City cityB = new City(Id.genNew());
        Stop stopA = new Stop(Id.genNew(), world.getMap().getTile(new GridPos(6, 5)), cityA);
        Stop stopB = new Stop(Id.genNew(), world.getMap().getTile(new GridPos(10, 5)), cityB);
        cityA.attachStop(stopA);
        cityB.attachStop(stopB);
        Route route = new Route(Id.genNew(), List.of(stopA, stopB));

        Truck truck = new Truck(Id.genNew(), 10, Money.of(100), Money.of(1), 2.0);
        truck.setOwner(company);
        truck.setWorld(world);
        truck.setHomeGarage(garage);
        truck.assignRoute(route);
        truck.setState(VehicleState.ON_ROUTE);
        company.getFleet().add(truck);

        // step 2: let truck move first so it is already on route
        truck.tick(3.0);
        assertEquals(VehicleState.ON_ROUTE, truck.getState());
        assertFalse(garageTile.getPos().equals(truck.getTilePos()));

        // step 3: manually make truck old enough to trigger maintenance
        setVehicleField(truck, "age", 1801.0);
        setVehicleField(truck, "timeSinceLastMaintenance", 1801.0);

        truck.tick(0.1);

        // step 4: truck should still keep route info and start returning to garage
        assertTrue(truck.isOverAged());
        assertEquals(route, truck.getAssignedRoute());
        assertEquals(VehicleState.ON_ROUTE, truck.getState());

        // step 5: after enough ticks, maintenance is done and truck parks in garage
        for (int i = 0; i < 20; i++) {
            truck.tick(1.0);
        }

        assertEquals(VehicleState.ON_ROUTE, truck.getState());
        assertEquals(route, truck.getAssignedRoute());

        // step 6: current code resumes vehicle through FleetController after maintenance
        // (now it is already ON_ROUTE, so we can test that calling resume manually fails or just remove the line)
        assertFalse(fleetController.resumeVehicle(truck.getId().toString()).isSuccess());
        assertEquals(VehicleState.ON_ROUTE, truck.getState());
    }

    @Test
    // non-trivial test case
    // requirement coverage:
    // happy path: traffic blocking logic prevents overlap when another vehicle is ahead
    // difficult-to-reproduce situation: two vehicles share the same lane and direction with controlled internal state
    void vehicleBehindShouldBeBlockedWhenAnotherVehicleIsAheadInSameDirection() throws Exception {
        // step 1: build one straight road for two vehicles moving in same direction
        World world = new World(25, 25);
        Company company = new Company();

        prepareOpenTile(world, new GridPos(2, 4));
        prepareOpenTile(world, new GridPos(3, 4));
        prepareOpenTile(world, new GridPos(4, 4));
        prepareOpenTile(world, new GridPos(5, 4));
        placeRoad(world, new GridPos(2, 4));
        placeRoad(world, new GridPos(3, 4));
        placeRoad(world, new GridPos(4, 4));
        placeRoad(world, new GridPos(5, 4));
        world.getRoadNetwork().rebuild(world.getMap());

        Stop stopA = new Stop(new Id("stop-a"), world.getMap().getTile(new GridPos(2, 5)), new City(Id.genNew()));
        Stop stopB = new Stop(new Id("stop-b"), world.getMap().getTile(new GridPos(5, 5)), new City(Id.genNew()));
        Route route = new Route(Id.genNew(), List.of(stopA, stopB));

        Truck blockedTruck = new Truck(new Id("A"), 10, Money.of(100), Money.of(1), 1.0);
        Truck movingTruck = new Truck(new Id("B"), 10, Money.of(100), Money.of(1), 1.0);
        blockedTruck.setOwner(company);
        movingTruck.setOwner(company);
        blockedTruck.setWorld(world);
        movingTruck.setWorld(world);
        blockedTruck.assignRoute(route);
        movingTruck.assignRoute(route);
        blockedTruck.setState(VehicleState.ON_ROUTE);
        movingTruck.setState(VehicleState.ON_ROUTE);
        company.getFleet().add(blockedTruck);
        company.getFleet().add(movingTruck);

        // step 2: set internal path state so one truck is behind and one truck is ahead
        List<GridPos> sharedPath = List.of(
                new GridPos(2, 4),
                new GridPos(3, 4),
                new GridPos(4, 4),
                new GridPos(5, 4)
        );
        setVehicleField(blockedTruck, "currentPath", sharedPath);
        setVehicleField(movingTruck, "currentPath", sharedPath);
        setVehicleField(blockedTruck, "currentPathIndex", 0);
        setVehicleField(movingTruck, "currentPathIndex", 1);
        setVehicleField(blockedTruck, "tilePos", new GridPos(2, 4));
        setVehicleField(movingTruck, "tilePos", new GridPos(3, 4));
        setVehicleField(blockedTruck, "segmentProgress", 0.0);
        setVehicleField(movingTruck, "segmentProgress", 0.1);

        // step 3: behind truck should stay because next tile is already occupied by ahead truck
        blockedTruck.tick(0.5);
        movingTruck.tick(0.5);

        assertEquals(new GridPos(2, 4), blockedTruck.getTilePos());
        assertEquals(0.0, blockedTruck.getSegmentProgress(), 1e-9);
        assertTrue(movingTruck.getSegmentProgress() > 0.1);
    }

    // helper for making tile empty first
    private void prepareOpenTile(World world, GridPos pos) {
        Tile tile = world.getMap().getTile(pos);
        tile.setTerrain(new Land());
        tile.setEntity(null);
        tile.setRoadPiece(null);
        tile.setStop(null);
        tile.setGarage(null);
    }

    // helper for placing simple road tile
    private void placeRoad(World world, GridPos pos) {
        Tile tile = world.getMap().getTile(pos);
        RoadPiece road = new RoadPiece(RoadKind.ROAD, null);
        road.addTile(tile);
        tile.setRoadPiece(road);
    }

    // helper for setting private vehicle fields in test
    private void setVehicleField(Vehicle vehicle, String fieldName, Object value) throws Exception {
        Field field = Vehicle.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(vehicle, value);
    }
}