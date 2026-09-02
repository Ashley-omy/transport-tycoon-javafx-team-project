package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameMapTest {

    @Test
    void constructorShouldInitializeTiles() {
        GameMap map = new GameMap(10, 10);
        assertEquals(10, map.getWidth());
        assertEquals(10, map.getHeight());
        assertNotNull(map.getTile(new common.GridPos(0, 0)));
        assertNotNull(map.getTile(new common.GridPos(9, 9)));
    }

    @Test
    void inBoundsShouldReturnCorrectly() {
        GameMap map = new GameMap(10, 10);
        assertTrue(map.inBounds(new common.GridPos(0, 0)));
        assertTrue(map.inBounds(new common.GridPos(9, 9)));
        assertFalse(map.inBounds(new common.GridPos(-1, 0)));
        assertFalse(map.inBounds(new common.GridPos(0, -1)));
        assertFalse(map.inBounds(new common.GridPos(10, 10)));
        assertFalse(map.inBounds(null));
    }

    @Test
    void setTerrainShouldWorkInBounds() {
        GameMap map = new GameMap(10, 10);
        common.GridPos pos = new common.GridPos(5, 5);
        Terrain terrain = new Water(WaterType.LAKE);
        map.setTerrain(pos, terrain);
        assertEquals(terrain, map.getTile(pos).getTerrain());
    }

    @Test
    void setTerrainShouldThrowOutOfBounds() {
        GameMap map = new GameMap(10, 10);
        assertThrows(IllegalArgumentException.class, () -> map.setTerrain(new common.GridPos(10, 10), new Land()));
    }
}
