package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GoodsTypeTest {

    @Test
    void enumValuesExist() {
        assertNotNull(GoodsType.valueOf("STEEL"));
        assertNotNull(GoodsType.valueOf("IRON"));
        assertNotNull(GoodsType.valueOf("WOOD"));
        assertNotNull(GoodsType.valueOf("PAPER"));
    }
}
