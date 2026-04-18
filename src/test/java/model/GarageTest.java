package model;

import common.GridPos;
import common.Id;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GarageTest {

    @Test
    void constructorShouldStockTwoBusesAndTwoTrucks() {
        Tile tile = new Tile(new GridPos(1, 1), new Land());

        Garage garage = new Garage(Id.genNew(), 10, 2, List.of(tile));

        assertEquals(4, garage.getVehicles().size());
        assertEquals(2, garage.getVehicles().stream().filter(Bus.class::isInstance).count());
        assertEquals(2, garage.getVehicles().stream().filter(Truck.class::isInstance).count());
        assertTrue(garage.getVehicles().stream().allMatch(vehicle -> vehicle.getHomeGarage() == garage));
    }

    @Test
    void constructorShouldRejectCapacityBelowInitialStockSize() {
        Tile tile = new Tile(new GridPos(1, 1), new Land());

        try {
            new Garage(Id.genNew(), 3, 1, List.of(tile));
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("capacity must be >= 4"));
            return;
        }

        throw new AssertionError("Expected IllegalArgumentException");
    }
}
