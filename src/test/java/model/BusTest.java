package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BusTest {

    @Test
    void busShouldOnlyAcceptPassengers() {
        Bus bus = new Bus(common.Id.genNew(), 50, common.Money.of(100), common.Money.of(5), 2.0);
        assertTrue(bus.acceptsKind(ShipmentKind.PASSENGERS));
        assertFalse(bus.acceptsKind(ShipmentKind.GOODS));
    }
}
