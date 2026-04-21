package model;

import common.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BridgeSpecTest {

    @Test
    void constructorStoresAllFields() {
        BridgeSpec spec = new BridgeSpec(BridgeType.TYPE_B, 6, Money.of(900), 1.5);

        assertEquals(BridgeType.TYPE_B, spec.getType());
        assertEquals(6, spec.getMaxSpanTiles());
        assertEquals(Money.of(900), spec.getCost());
        assertEquals(1.5, spec.getSpeedLimit(), 0.0);
    }

    @Test
    void constructorRejectsNullType() {
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(null, 4, Money.of(100), 1.0));
    }

    @Test
    void constructorRejectsZeroMaxSpanTiles() {
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(BridgeType.TYPE_A, 0, Money.of(100), 1.0));
    }

    @Test
    void constructorRejectsNegativeMaxSpanTiles() {
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(BridgeType.TYPE_A, -1, Money.of(100), 1.0));
    }

    @Test
    void constructorRejectsNullCost() {
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(BridgeType.TYPE_A, 4, null, 1.0));
    }

    @Test
    void constructorRejectsNegativeCost() {
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(BridgeType.TYPE_A, 4, Money.of(-1), 1.0));
    }

    @Test
    void constructorRejectsZeroSpeedLimit() {
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(BridgeType.TYPE_A, 4, Money.of(100), 0.0));
    }

    @Test
    void constructorRejectsNegativeSpeedLimit() {
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(BridgeType.TYPE_A, 4, Money.of(100), -1.0));
    }
}
