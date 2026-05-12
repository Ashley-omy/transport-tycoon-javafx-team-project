package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WaterTest {

    @Test
    void overridesShouldReturnCorrectValues() {
        Water water = new Water(WaterType.LAKE);
        assertTrue(water.isWater());
        assertFalse(water.isLand());
        assertFalse(water.isPassable());
        assertEquals(2.0, water.buildMultiplier());
        assertEquals(WaterType.LAKE, water.getWaterType());
    }
}
