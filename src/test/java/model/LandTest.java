package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LandTest {

    @Test
    void overridesShouldReturnCorrectValues() {
        Land land = new Land();
        assertTrue(land.isLand());
        assertFalse(land.isWater());
        assertTrue(land.isPassable());
        assertEquals(1.0, land.buildMultiplier());
    }
}
