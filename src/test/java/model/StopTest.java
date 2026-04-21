package model;

import common.GridPos;
import common.Id;
import common.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StopTest {

    @Test
    void enqueueAllowsValidShipmentAndItCanBeDequeuedLater() {
        Stop stop = new Stop(
                Id.genNew(),
                new Tile(new GridPos(1, 1), new Land()),
                new City(Id.genNew())
        );
        Shipment shipment = new Shipment(
                ShipmentKind.GOODS,
                GoodsType.WOOD,
                20,
                Id.genNew(),
                Id.genNew(),
                Money.of(4)
        );
        Truck truck = new Truck(Id.genNew(), 100, Money.of(100), Money.of(1), 1.0);

        stop.enqueue(shipment);
        Shipment dequeued = stop.dequeueFor(truck);

        assertTrue(dequeued != null);
        assertEquals(20, dequeued.getUnits());
        assertFalse(stop.dequeueFor(truck) != null);
    }

    @Test
    void enqueueRejectsNullShipment() {
        Stop stop = new Stop(
                Id.genNew(),
                new Tile(new GridPos(1, 1), new Land()),
                new City(Id.genNew())
        );

        assertThrows(IllegalArgumentException.class, () -> stop.enqueue(null));
    }
}
