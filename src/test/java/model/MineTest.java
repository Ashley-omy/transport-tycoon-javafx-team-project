package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MineTest {
    @Test
    void createIronMineShouldInitializeCorrectly() {
        Mine mine = Mine.createIronMine(common.Id.genNew());
        assertNull(mine.getInputType());
        assertEquals(GoodsType.IRON, mine.getOutputType());
        assertEquals(200, mine.getMaxStockCapacity());
        assertEquals(20, mine.getProductionRate());
        assertEquals(5.0, mine.getProductionTime());
    }

    @Test
    void createWoodMineShouldInitializeCorrectly() {
        Mine mine = Mine.createWoodMine(common.Id.genNew());
        assertNull(mine.getInputType());
        assertEquals(GoodsType.WOOD, mine.getOutputType());
        assertEquals(150, mine.getMaxStockCapacity());
        assertEquals(15, mine.getProductionRate());
        assertEquals(4.0, mine.getProductionTime());
    }
}
