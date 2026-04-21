package model;

import common.GridPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldTest {

    @Test
    void initializesDefaultBridgeCatalog() {
        World world = new World(30, 30);

        assertEquals(3, world.getBridgeCatalog().size());
        assertEquals(BridgeType.TYPE_A, world.getBridgeCatalog().get(0).getType());
        assertEquals(BridgeType.TYPE_B, world.getBridgeCatalog().get(1).getType());
        assertEquals(BridgeType.TYPE_C, world.getBridgeCatalog().get(2).getType());
    }

    @Test
    void buildsBridgeUsingCatalogSpec() {
        World world = new World(30, 30);
        List<GridPos> line = List.of(
                new GridPos(0, 0),
                new GridPos(1, 0),
                new GridPos(2, 0)
        );

        world.getMap().setTerrain(new GridPos(1, 0), new Water(WaterType.RIVER));
        world.getMap().getTile(new GridPos(0, 0)).setRoadPiece(null);
        world.getMap().getTile(new GridPos(1, 0)).setRoadPiece(null);
        world.getMap().getTile(new GridPos(2, 0)).setRoadPiece(null);

        world.buildBridge(line, BridgeType.TYPE_A);

        RoadPiece first = world.getMap().getTile(new GridPos(0, 0)).getRoadPiece();
        RoadPiece second = world.getMap().getTile(new GridPos(1, 0)).getRoadPiece();
        RoadPiece third = world.getMap().getTile(new GridPos(2, 0)).getRoadPiece();
        BridgeSpec spec = first.getBridgeSpec();

        assertNotNull(spec);
        assertEquals(RoadKind.BRIDGE, first.getKind());
        assertEquals(BridgeType.TYPE_A, spec.getType());
        assertSame(first, second);
        assertSame(first, third);
        assertEquals(3, first.getOccupiedTiles().size());
    }

    @Test
    void rejectsBridgeLongerThanSelectedSpec() {
        World world = new World(30, 30);
        List<GridPos> line = List.of(
                new GridPos(0, 0),
                new GridPos(1, 0),
                new GridPos(2, 0),
                new GridPos(3, 0),
                new GridPos(4, 0)
        );

        for (GridPos pos : line) {
            world.getMap().getTile(pos).setRoadPiece(null);
            world.getMap().setTerrain(pos, new Water(WaterType.RIVER));
        }

        assertThrows(IllegalArgumentException.class, () -> world.buildBridge(line, BridgeType.TYPE_A));
    }

    @Test
    void rejectsBridgeLineWithoutWater() {
        World world = new World(30, 30);
        List<GridPos> line = List.of(
                new GridPos(0, 0),
                new GridPos(1, 0)
        );

        world.getMap().getTile(new GridPos(0, 0)).setRoadPiece(null);
        world.getMap().getTile(new GridPos(1, 0)).setRoadPiece(null);

        assertThrows(IllegalArgumentException.class, () -> world.buildBridge(line, BridgeType.TYPE_A));
    }

}
