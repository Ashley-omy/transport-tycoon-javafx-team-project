package model;

import common.GridPos;
import common.Id;
import common.Money;
import controller.FleetController;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void truckRoutesMineCargoToFactoryMergesAndPaysOutOnDelivery() {
        World world = new World(25, 25);
        Company company = new Company();
        company.setWorld(world);

        Mine mine = Mine.createIronMine(Id.genNew());
        Factory steelMill = Factory.createSteelMill(Id.genNew());

        Stop stopA = new Stop(Id.genNew(), new Tile(new GridPos(1, 1), new Land()), mine);
        Stop stopB = new Stop(Id.genNew(), new Tile(new GridPos(2, 1), new Land()), steelMill);
        mine.attachStop(stopA);
        steelMill.attachStop(stopB);

        Route route = new Route(Id.genNew(), List.of(stopA, stopB));

        Truck truck = new Truck(Id.genNew(), 15, Money.of(100), Money.of(1), 1.0);
        truck.setWorld(world);
        truck.setOwner(company);
        truck.assignRoute(route);

        stopA.enqueue(new Shipment(ShipmentKind.GOODS, GoodsType.IRON, 4, stopA.getId(), stopA.getId(), Money.of(3)));
        stopA.enqueue(new Shipment(ShipmentKind.GOODS, GoodsType.IRON, 6, stopA.getId(), stopA.getId(), Money.of(3)));

        Money cashBefore = company.getEconomy().getCash();

        // First tick initializes the route at stop A and performs unload/load there.
        truck.tick(0.1);

        Shipment loadedCargo = truck.getCargo();
        assertNotNull(loadedCargo);
        assertEquals(10, loadedCargo.getUnits());
        assertEquals(stopB.getId(), loadedCargo.getToStopId());
        assertEquals(5, truck.getFreeCapacityUnits());

        Money payout = truck.unloadTo(stopB);

        assertEquals(Money.of(30), payout);
        assertEquals(10, steelMill.getInputStock());
        assertEquals(15, truck.getFreeCapacityUnits());
        assertEquals(cashBefore.add(Money.of(30)), company.getEconomy().getCash());
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
