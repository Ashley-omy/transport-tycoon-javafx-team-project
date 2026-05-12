package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FacilityTest {

    private static class TestFacility extends Facility {
        public TestFacility(common.Id id, GoodsType inputType, GoodsType outputType, int maxStockCapacity, int productionRate, double productionTime) {
            super(id, inputType, outputType, maxStockCapacity, productionRate, productionTime);
        }
    }

    @Test
    void constructorShouldStoreFields() {
        TestFacility facility = new TestFacility(common.Id.genNew(), GoodsType.WOOD, GoodsType.PAPER, 100, 10, 5.0);
        assertEquals(GoodsType.WOOD, facility.getInputType());
        assertEquals(GoodsType.PAPER, facility.getOutputType());
        assertEquals(100, facility.getMaxStockCapacity());
        assertEquals(10, facility.getProductionRate());
        assertEquals(5.0, facility.getProductionTime());
        assertEquals(0, facility.getInputStock());
        assertEquals(0, facility.getOutputStock());
    }

    @Test
    void acceptDeliveryShouldIncreaseInputStock() {
        TestFacility facility = new TestFacility(common.Id.genNew(), GoodsType.WOOD, GoodsType.PAPER, 100, 10, 5.0);
        Shipment shipment = new Shipment(ShipmentKind.GOODS, GoodsType.WOOD, 20, common.Id.genNew(), common.Id.genNew(), common.Money.of(1));
        facility.acceptDelivery(shipment);
        assertEquals(20, facility.getInputStock());
    }

    @Test
    void tickShouldProduceOutputOverTime() {
        TestFacility facility = new TestFacility(common.Id.genNew(), GoodsType.WOOD, GoodsType.PAPER, 100, 10, 5.0);
        Shipment shipment = new Shipment(ShipmentKind.GOODS, GoodsType.WOOD, 20, common.Id.genNew(), common.Id.genNew(), common.Money.of(1));
        facility.acceptDelivery(shipment);
        
        facility.tick(5.0); // should complete one cycle
        
        assertEquals(10, facility.getInputStock());
        assertEquals(10, facility.getOutputStock());
    }
}
