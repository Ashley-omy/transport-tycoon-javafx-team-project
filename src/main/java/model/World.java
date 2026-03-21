package model;

import common.GridPos;
import common.Money;
import common.Id;

import java.util.ArrayList;
import java.util.List;

public class World {

    private GameMap map;
    private RoadNetwork roads;
    private List<BridgeSpec> bridgeCatalog = new ArrayList<>();
    long rngSeed;

    // later need to modify (bridge + rngspeed)
    public World(int width, int height){

        this.map = new GameMap(width, height);
        this.roads = new RoadNetwork();

        placeInitialEntities();
    }

    // place city(3*3) / facility(2*2) FOOTPRINT logic
    public void placeEntity(MapEntity entity, GridPos center) {
        int w = entity.getFootprintW();
        int startOffset = -(w / 2);
        int endOffset = startOffset + w - 1;

        for (int dx = startOffset; dx <= endOffset; dx++) {
            for (int dy = startOffset; dy <= endOffset; dy++) {

                GridPos p = new GridPos(center.x + dx, center.y + dy);

                if (!map.inBounds(p)) {
                    throw new IllegalArgumentException("Entity footprint out of bounds: " + p);
                }

                Tile t = map.getTile(p);

                if (t.getEntity() != null) {
                    throw new IllegalStateException("Tile already occupied at: " + p);
                }

                t.setEntity(entity);
                entity.getOccupiedTiles().add(t);
            }
        }
    }

    // for place some cities and facilities for MS2
    private void placeInitialEntities() {
        City c1 = new City(Id.genNew());
        placeEntity(c1, new GridPos(5, 5));

        Facility steelMill = Factory.createSteelMill(Id.genNew());
        placeEntity(steelMill, new GridPos(10, 10));

        Facility ironMine = Mine.createIronMine(Id.genNew());
        placeEntity(ironMine, new GridPos(15, 15));
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
