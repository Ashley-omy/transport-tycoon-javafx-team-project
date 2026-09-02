package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MapEntityTest {
    
    private static class TestMapEntity extends MapEntity {
        public TestMapEntity(common.Id id, int footprintW) {
            super(id, footprintW);
        }
        
        @Override
        public void tick(double deltaTime) {
        }
    }

    @Test
    void constructorShouldStoreFields() {
        common.Id id = common.Id.genNew();
        TestMapEntity entity = new TestMapEntity(id, 2);
        
        assertEquals(id, entity.getId());
        assertEquals(2, entity.getFootprintW());
        assertNotNull(entity.getOccupiedTiles());
        assertTrue(entity.getOccupiedTiles().isEmpty());
    }

    @Test
    void eventDisplaysShouldWork() {
        TestMapEntity entity = new TestMapEntity(common.Id.genNew(), 1);
        
        entity.pushEventDisplay("Hello", 5.0);
        assertEquals(1, entity.getActiveEventDisplayTexts().size());
        assertEquals("Hello", entity.getActiveEventDisplayTexts().get(0));
        
        entity.tickEventDisplays(2.0);
        assertEquals(1, entity.getActiveEventDisplayTexts().size());
        
        entity.tickEventDisplays(3.0);
        assertTrue(entity.getActiveEventDisplayTexts().isEmpty());
    }
}
