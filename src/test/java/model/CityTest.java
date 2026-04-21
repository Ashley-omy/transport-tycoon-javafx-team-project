package model;

import org.junit.jupiter.api.Test;

import common.GridPos;
import common.Id;
import common.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CityTest {

    @Test
    void tickIncreasesDemandAndPassengersAreSplitEvenlyAcrossStops() {
        City city = new City(Id.genNew());

        Stop stopA = new Stop(Id.genNew(), new Tile(new GridPos(1, 1), new Land()), city);
        Stop stopB = new Stop(Id.genNew(), new Tile(new GridPos(2, 1), new Land()), city);
        city.attachStop(stopA);
        city.attachStop(stopB);

        int steelDemandBefore = city.getGoodsDemand(GoodsType.STEEL);
        int paperDemandBefore = city.getGoodsDemand(GoodsType.PAPER);
        int passengerDemandBefore = city.getPassengerDemand();

        city.tick(4.0);
        city.emitSupplyToStops();

        assertTrue(city.getGoodsDemand(GoodsType.STEEL) > steelDemandBefore);
        assertTrue(city.getGoodsDemand(GoodsType.PAPER) > paperDemandBefore);
        assertTrue(city.getPassengerDemand() > passengerDemandBefore);

        Bus busA = new Bus(Id.genNew(), 1000, Money.of(100), Money.of(1), 1.0);
        Bus busB = new Bus(Id.genNew(), 1000, Money.of(100), Money.of(1), 1.0);

        Shipment emittedA = stopA.dequeueFor(busA);
        Shipment emittedB = stopB.dequeueFor(busB);

        assertNotNull(emittedA);
        assertNotNull(emittedB);
        assertEquals(ShipmentKind.PASSENGERS, emittedA.getKind());
        assertEquals(ShipmentKind.PASSENGERS, emittedB.getKind());

        int expectedPerStop = (city.getPopulation() / 10) / 2;
        assertEquals(expectedPerStop, emittedA.getUnits());
        assertEquals(expectedPerStop, emittedB.getUnits());
    }
}
