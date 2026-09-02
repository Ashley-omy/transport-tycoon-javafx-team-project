package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TerrainTest {

    private static class TestTerrain extends Terrain {
        @Override
        public boolean isPassable() {
            return true;
        }

        @Override
        public double buildMultiplier() {
            return 1.0;
        }
    }

    @Test
    void addAndRemoveOccupiedTile() {
        TestTerrain terrain = new TestTerrain();
        Tile tile = new Tile(new common.GridPos(0, 0), terrain);
        
        terrain.addOccupiedTile(tile);
        assertEquals(1, terrain.getOccupiedTiles().size());
        assertTrue(terrain.getOccupiedTiles().contains(tile));
        
        terrain.removeOccupiedTile(tile);
        assertTrue(terrain.getOccupiedTiles().isEmpty());
    }

    @Test
    void defaultMethodsReturnCorrectly() {
        TestTerrain terrain = new TestTerrain();
        assertFalse(terrain.isWater());
        assertFalse(terrain.isLand());
        assertFalse(terrain.isForest());
    }
}
