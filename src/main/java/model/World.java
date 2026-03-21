package model;

import common.GridPos;
import common.Money;
import common.Id;

import java.util.List;

public class World {

    private GameMap map;
    private RoadNetwork roads;
    private List<BridgeSpec> bridgeCatalog;
    long rngSeed;

    // later need to modify (bridge + rngspeed)
    public World(int width, int height){

        this.map = new GameMap(width, height);
        roads = new RoadNetwork();
    }

    public static final Money ROAD_BUILD_COST = Money.of(150);

    public boolean canBuildAt(GridPos pos) {
        if (!map.inBounds(pos)) return false;

        Tile t = map.getTile(pos);

        return t.getRoadPiece() == null &&
                t.getStop() == null &&
                t.getGarage() == null &&
                t.getEntity() == null;
    }
    public GameMap getMap(){ return map;}

    public RoadNetwork getRoadNetwork() { return roads; }

    public List<BridgeSpec> getBridgeCatalog() { return bridgeCatalog; }

    public void buildRoad(GridPos pos) {
        Tile tile = map.getTile(pos);

        RoadPiece piece = new RoadPiece(RoadKind.ROAD, null);
        piece.addTile(tile);
        tile.setRoadPiece(piece);

        roads.rebuild(map);
    }

    public void buildStop(GridPos pos, MapEntity servedPlace) {
        Tile tile = map.getTile(pos);

        Stop stop = new Stop(Id.genNew(), tile, servedPlace);
        tile.setStop(stop);
        servedPlace.attachStop(stop);
    }

    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;
    }
}