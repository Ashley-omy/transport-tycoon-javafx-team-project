package model;

import org.junit.jupiter.api.Test;

import common.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadPieceTest {

    @Test
    void roadPieceCannotCarryBridgeSpec() {
        BridgeSpec spec = new BridgeSpec(BridgeType.TYPE_A, 4, Money.of(100), 1.0);

        assertThrows(IllegalArgumentException.class, () -> new RoadPiece(RoadKind.ROAD, spec));
    }

    @Test
    void bridgePieceRequiresBridgeSpec() {
        assertThrows(IllegalArgumentException.class, () -> new RoadPiece(RoadKind.BRIDGE, null));
    }

    @Test
    void bridgePieceStoresBridgeSpec() {
        BridgeSpec spec = new BridgeSpec(BridgeType.TYPE_B, 7, Money.of(200), 1.5);

        RoadPiece piece = new RoadPiece(RoadKind.BRIDGE, spec);

        assertEquals(RoadKind.BRIDGE, piece.getKind());
        assertEquals(spec, piece.getBridgeSpec());
        assertTrue(piece.isBridge());
    }

    @Test
    void roadPieceHasNoBridgeSpec() {
        RoadPiece piece = new RoadPiece(RoadKind.ROAD, null);

        assertEquals(RoadKind.ROAD, piece.getKind());
        assertNull(piece.getBridgeSpec());
    }
}
