package model;
import common.GridPos;

import java.util.Objects;

public class Tile implements java.io.Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final GridPos pos;
    private Terrain terrain;   // #163  Implement tile terrain field
    private RoadPiece road;    // # 166 Implement tile roadPiece field
    private Stop stop;         // #165  Implement tile stop field
    private Garage garage;
    private MapEntity entity;  // #164 Implement tile entity field

    public Tile(GridPos pos, Terrain terrain) {
        this.pos = pos;
        setTerrain(terrain);
    }

    public GridPos getPos() { return pos; }
    public Terrain getTerrain() { return terrain; }
    public void setTerrain(Terrain terrain) {
        Terrain nextTerrain = Objects.requireNonNull(terrain, "terrain");
        if (this.terrain != null) {
            this.terrain.removeOccupiedTile(this);
        }
        this.terrain = nextTerrain;
        this.terrain.addOccupiedTile(this);
    }

    public MapEntity getEntity() { return entity; }
    public void setEntity(MapEntity e) { this.entity = e; }

    public Stop getStop() { return stop; }
    public void setStop(Stop s) { this.stop = s; }

    public Garage getGarage() { return garage; }
    public void setGarage(Garage g) { this.garage = g; }

    public RoadPiece getRoadPiece() { return road; }
    public void setRoadPiece(RoadPiece r) { this.road = r; }

    public boolean hasEntity() { return entity != null; }
    public boolean hasStop() { return stop != null; }
    public boolean hasGarage() { return garage != null; }
    public boolean hasRoad() { return road != null; }

    public boolean isPassable() {
        return terrain.isPassable();
    }

    public Facility getFacility() {
        return (entity instanceof Facility) ? (Facility) entity : null;
    }

    public boolean isWater() {
        return terrain.isWater();
    }

    public boolean isForest() {
        return terrain.isForest();
    }
}