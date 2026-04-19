package model;

import common.GridPos;
import common.Id;
import common.Money;
import controller.FleetController;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleTest {

    @Test
    void bridgeSpeedLimitSlowsVehicleMovement() {
        World world = new World(30, 30);

        GridPos roadLeft = new GridPos(0, 0);
        GridPos bridgeMid = new GridPos(1, 0);
        GridPos roadRight = new GridPos(2, 0);
        GridPos stopLeftPos = new GridPos(0, 1);
        GridPos stopRightPos = new GridPos(2, 1);

        world.buildRoad(roadLeft);
        world.buildRoad(roadRight);
        world.getMap().setTerrain(bridgeMid, new Water(WaterType.RIVER));
        world.buildBridge(List.of(bridgeMid), BridgeType.TYPE_A);

        Stop leftStop = new Stop(Id.genNew(), world.getMap().getTile(stopLeftPos), new City(Id.genNew()));
        Stop rightStop = new Stop(Id.genNew(), world.getMap().getTile(stopRightPos), new City(Id.genNew()));
        Route route = new Route(Id.genNew(), List.of(leftStop, rightStop));

        Truck truck = new Truck(Id.genNew(), 10, Money.of(100), Money.of(1), 2.0);
        truck.setWorld(world);
        truck.assignRoute(route);

        truck.tick(2.5);

        assertEquals(new GridPos(0, 0), truck.getTilePos());
        assertTrue(truck.getWorldPos().x > 0.9);
        assertTrue(truck.getWorldPos().x < 1.1);
    }

    @Test
    void busDeliveryIncreasesCompanyCash() {
        World world = new World(25, 25);
        Company company = new Company();
        company.setWorld(world);
        FleetController fleet = new FleetController(company, world);

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

        City cityA = new City(Id.genNew());
        City cityB = new City(Id.genNew());
        Stop stopA = new Stop(Id.genNew(), world.getMap().getTile(new GridPos(6, 5)), cityA);
        Stop stopB = new Stop(Id.genNew(), world.getMap().getTile(new GridPos(7, 5)), cityB);
        cityA.attachStop(stopA);
        cityB.attachStop(stopB);

        Route route = new Route(Id.genNew(), List.of(stopA, stopB));

        Tile garageTile = world.getMap().getTile(new GridPos(5, 5));
        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(garageTile));
        garageTile.setGarage(garage);
        garage.setRoute(route);

        Vehicle bus = garage.getVehicles().stream().filter(v -> v instanceof Bus).findFirst().orElseThrow();
        assertTrue(fleet.purchaseVehicleInGarage(garage, bus).isSuccess());

        Money cashBeforeDelivery = company.getEconomy().getCash();
        stopA.enqueue(new Shipment(ShipmentKind.PASSENGERS, null, 10, stopA.getId(), stopA.getId(), Money.of(2)));

        for (int i = 0; i < 8; i++) {
            company.tick(0.5);
        }

        assertTrue(company.getEconomy().getCash().greaterThan(cashBeforeDelivery));
        assertEquals(Money.of(20), company.getEconomy().getCash().subtract(cashBeforeDelivery));
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
