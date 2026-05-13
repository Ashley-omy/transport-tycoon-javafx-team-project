package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehicleFactoryTest {

    @Test
    void createSmallTruckShouldInitializeCorrectly() {
        Truck truck = VehicleFactory.createSmallTruck(common.Id.genNew());
        assertEquals(50, truck.getCapacityUnits());
        assertEquals(common.Money.of(3_000), truck.getPurchaseCost());
        assertEquals(common.Money.of(5), truck.getMaintenanceCost());
        assertEquals(2.0, truck.getSpeed());
    }

    @Test
    void createLargeTruckShouldInitializeCorrectly() {
        Truck truck = VehicleFactory.createLargeTruck(common.Id.genNew());
        assertEquals(150, truck.getCapacityUnits());
        assertEquals(common.Money.of(8_000), truck.getPurchaseCost());
        assertEquals(common.Money.of(20), truck.getMaintenanceCost());
        assertEquals(1.0, truck.getSpeed());
    }

    @Test
    void createSmallBusShouldInitializeCorrectly() {
        Bus bus = VehicleFactory.createSmallBus(common.Id.genNew());
        assertEquals(30, bus.getCapacityUnits());
        assertEquals(common.Money.of(2_000), bus.getPurchaseCost());
        assertEquals(common.Money.of(4), bus.getMaintenanceCost());
        assertEquals(2.5, bus.getSpeed());
    }

    @Test
    void createLargeBusShouldInitializeCorrectly() {
        Bus bus = VehicleFactory.createLargeBus(common.Id.genNew());
        assertEquals(80, bus.getCapacityUnits());
        assertEquals(common.Money.of(5_000), bus.getPurchaseCost());
        assertEquals(common.Money.of(15), bus.getMaintenanceCost());
        assertEquals(1.5, bus.getSpeed());
    }
}
