package model;
import common.GridPos;

public class Tile {
    private final GridPos pos;
    private Terrain terrain;
    private RoadPiece road;
    private Stop stop;
    private Garage garage;
    private MapEntity entity;


    public Tile(GridPos pos, Terrain terrain) {
        this.pos = pos;
        this.terrain = terrain;
    }

    public GridPos getPos() {
        return pos;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public RoadPiece getRoadPiece() {
        return road;
    }

    public void setRoadPiece(RoadPiece road) {
        this.road = road;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public Garage getGarage() {
        return garage;
    }

    public void setGarage(Garage garage) {
        this.garage = garage;
    }

    public MapEntity getEntity() {
        return entity;
    }

    public void setEntity(MapEntity entity) {
        this.entity = entity;
    }
}
