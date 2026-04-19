package model;

import common.GridPos;
import common.Id;
import common.Money;
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
}
