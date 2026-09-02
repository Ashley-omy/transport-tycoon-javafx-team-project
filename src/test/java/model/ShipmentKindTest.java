package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShipmentKindTest {

    @Test
    void enumValuesExist() {
        assertNotNull(ShipmentKind.valueOf("GOODS"));
        assertNotNull(ShipmentKind.valueOf("PASSENGERS"));
    }
}
