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

    public Tile(GridPos pos) {
        this.pos = pos;
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
