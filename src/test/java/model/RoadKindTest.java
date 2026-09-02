package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoadKindTest {

    @Test
    void enumValuesExist() {
        assertNotNull(RoadKind.valueOf("ROAD"));
        assertNotNull(RoadKind.valueOf("BRIDGE"));
    }
}
