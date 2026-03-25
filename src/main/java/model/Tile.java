package model;
import common.GridPos;

import common.GridPos;
import model.Terrain;
import model.RoadPiece;
import model.Stop;
import model.Garage;
import model.MapEntity;

public class Tile {
    private GridPos pos;
    private Terrain terrain;   // #163  Implement tile terrain field
    private RoadPiece road;    // # 166 Implement tile roadPiece field
    private Stop stop;         // #165  Implement tile stop field
    private Garage garage;
    private MapEntity entity;  // #164 Implement tile entity field

    public Tile(GridPos pos, Terrain terrain) {
        this.pos = pos;
        this.terrain = terrain;
    }

    public GridPos getPos() { return pos; }
    public Terrain getTerrain() { return terrain; }

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
