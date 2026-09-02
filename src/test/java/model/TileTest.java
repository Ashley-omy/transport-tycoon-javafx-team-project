package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TileTest {

    @Test
    void constructorShouldStoreFieldsAndRegisterWithTerrain() {
        Terrain land = new Land();
        Tile tile = new Tile(new common.GridPos(5, 5), land);
        
        assertEquals(new common.GridPos(5, 5), tile.getPos());
        assertEquals(land, tile.getTerrain());
        assertTrue(land.getOccupiedTiles().contains(tile));
    }

    @Test
    void setTerrainShouldUnregisterAndRegister() {
        Terrain land = new Land();
        Terrain water = new Water(WaterType.RIVER);
        Tile tile = new Tile(new common.GridPos(5, 5), land);
        
        tile.setTerrain(water);
        
        assertEquals(water, tile.getTerrain());
        assertFalse(land.getOccupiedTiles().contains(tile));
        assertTrue(water.getOccupiedTiles().contains(tile));
    }

    @Test
    void settersAndGettersShouldWork() {
        Tile tile = new Tile(new common.GridPos(0, 0), new Land());
        
        City city = new City(common.Id.genNew());
        tile.setEntity(city);
        assertEquals(city, tile.getEntity());
        
        Stop stop = new Stop(common.Id.genNew(), tile, city);
        tile.setStop(stop);
        assertEquals(stop, tile.getStop());
        
        Garage garage = new Garage(common.Id.genNew(), 10, 2, java.util.List.of(tile));
        tile.setGarage(garage);
        assertEquals(garage, tile.getGarage());
        assertTrue(tile.hasGarage());
        
        RoadPiece road = new RoadPiece(RoadKind.ROAD, null);
        tile.setRoadPiece(road);
        assertEquals(road, tile.getRoadPiece());
        assertTrue(tile.hasRoad());
    }
}
