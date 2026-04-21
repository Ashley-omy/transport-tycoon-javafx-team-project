package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class BridgeTypeTest {

    @Test
    void exposesTheBridgeTypesFromTheUml() {
        assertArrayEquals(
                new BridgeType[]{BridgeType.TYPE_A, BridgeType.TYPE_B, BridgeType.TYPE_C},
                BridgeType.values()
        );
    }
}
