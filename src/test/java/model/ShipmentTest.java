package model;

import common.Id;
import common.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipmentTest {
    @Test
    void splitOffReturnsRequestedUnitsAndKeepsMetadata() {
        Id fromStopId = Id.genNew();
        Id toStopId = Id.genNew();
        Shipment shipment = new Shipment(
                ShipmentKind.GOODS,
                GoodsType.WOOD,
                100,
                fromStopId,
                toStopId,
                Money.of(5)
        );

        Shipment split = shipment.splitOff(30);

        assertEquals(30, split.getUnits());
        assertEquals(70, shipment.getUnits());
        assertEquals(GoodsType.WOOD, split.getGoodsType());
        assertEquals(fromStopId, split.getFromStopId());
        assertEquals(toStopId, split.getToStopId());
    }

    @Test
    void splitOffRejectsInvalidAmounts() {
        Shipment shipment = new Shipment(
                ShipmentKind.GOODS,
                GoodsType.WOOD,
                100,
                Id.genNew(),
                Id.genNew(),
                Money.of(5)
        );

        assertThrows(IllegalArgumentException.class, () -> shipment.splitOff(-1));
        assertThrows(IllegalArgumentException.class, () -> shipment.splitOff(0));
        assertThrows(IllegalArgumentException.class, () -> shipment.splitOff(150));
    }

    @Test
    void passengersShipmentRejectsGoodsTypeInConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new Shipment(
                ShipmentKind.PASSENGERS,
                GoodsType.WOOD,
                10,
                Id.genNew(),
                Id.genNew(),
                Money.of(2)
        ));
    }
}
