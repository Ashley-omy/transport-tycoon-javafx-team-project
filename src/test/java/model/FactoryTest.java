package model;

import org.junit.jupiter.api.Test;

import common.Id;
import common.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FactoryTest {

    @Test
    void productionCycleConsumesInputProducesOutputAndStopsAtFullCapacity() {
        // Checks: one full cycle consumes input and produces output, then production halts at full output capacity.
        Factory steelMill = Factory.createSteelMill(Id.genNew());
        Id stopId = Id.genNew();

        steelMill.acceptDelivery(new Shipment(
                ShipmentKind.GOODS,
                GoodsType.IRON,
                30,
                stopId,
                stopId,
                Money.of(1)
        ));

        int initialInput = steelMill.getInputStock();
        int productionRate = steelMill.getProductionRate();

        // Action: run exactly one production cycle.
        steelMill.tick(steelMill.getProductionTime());

        // Assert: consumed and produced amounts match productionRate.
        assertEquals(initialInput - productionRate, steelMill.getInputStock());
        assertEquals(productionRate, steelMill.getOutputStock());

        // Edge case: with full output stock, further ticks must not consume input.
        steelMill.outputStock = steelMill.getMaxStockCapacity();
        int inputBeforeBlockedTick = steelMill.getInputStock();

        steelMill.tick(steelMill.getProductionTime() * 2.0);

        assertEquals(steelMill.getMaxStockCapacity(), steelMill.getOutputStock());
        assertEquals(inputBeforeBlockedTick, steelMill.getInputStock());
    }
}
