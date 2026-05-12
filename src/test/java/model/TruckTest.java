package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TruckTest {

    @Test
    void truckShouldOnlyAcceptGoods() {
        Truck truck = new Truck(common.Id.genNew(), 50, common.Money.of(100), common.Money.of(5), 2.0);
        assertTrue(truck.acceptsKind(ShipmentKind.GOODS));
        assertFalse(truck.acceptsKind(ShipmentKind.PASSENGERS));
    }
}
