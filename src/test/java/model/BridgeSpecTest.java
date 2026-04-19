package model;

import common.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BridgeSpecTest {

    @Test
    void storesAllBridgeSpecFields() {
        BridgeSpec spec = new BridgeSpec(BridgeType.TYPE_B, 6, Money.of(900), 1.5);

        assertEquals(BridgeType.TYPE_B, spec.getType());
        assertEquals(6, spec.getMaxSpanTiles());
        assertEquals(Money.of(900), spec.getCost());
        assertEquals(1.5, spec.getSpeedLimit());
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(null, 4, Money.of(100), 1.0));
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(BridgeType.TYPE_A, 0, Money.of(100), 1.0));
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(BridgeType.TYPE_A, 4, null, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(BridgeType.TYPE_A, 4, Money.of(-1), 1.0));
        assertThrows(IllegalArgumentException.class, () -> new BridgeSpec(BridgeType.TYPE_A, 4, Money.of(100), 0.0));
    }

    @Test
    void comparesByValue() {
        BridgeSpec first = new BridgeSpec(BridgeType.TYPE_C, 8, Money.of(2_000), 2.25);
        BridgeSpec second = new BridgeSpec(BridgeType.TYPE_C, 8, Money.of(2_000), 2.25);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
