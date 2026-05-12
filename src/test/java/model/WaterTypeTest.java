package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WaterTypeTest {

    @Test
    void enumValuesExist() {
        assertNotNull(WaterType.valueOf("RIVER"));
        assertNotNull(WaterType.valueOf("LAKE"));
    }
}
