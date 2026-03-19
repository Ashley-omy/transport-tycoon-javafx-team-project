/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import common.*;
import model.*;
import java.util.ArrayList;
import java.util.List;

public class BuildController {
    private final GameMap gameMap;
    private final RoadNetwork roadNetwork;

    public BuildController(GameMap gameMap, RoadNetwork roadNetwork) {
        this.gameMap = gameMap;
        this.roadNetwork = roadNetwork;
    }

    //--------- road build rules --------------------

    // for if user is allowed to build road
    // step 1: we wanna get the neighbors of the tile
    private List<Tile> getNeighbors(Tile tile) {
        List<Tile> list = new ArrayList<>();
        int x = tile.getPos().x;
        int y = tile.getPos().y;

        GridPos right = new GridPos(x+1, y);
        GridPos left = new GridPos(x-1, y);
        GridPos up = new GridPos(x, y+1);
        GridPos down = new GridPos(x, y-1);

        if (gameMap.inBounds(right)) list.add(gameMap.getTile(right));
        if (gameMap.inBounds(left)) list.add(gameMap.getTile(left));
        if (gameMap.inBounds(up)) list.add(gameMap.getTile(up));
        if (gameMap.inBounds(down)) list.add(gameMap.getTile(down));

        return list;
    }

    // step 2: do we aleady have existing roads on map?
    // if no, its fine to build at random place
    // if yes, we make sure new one connect to existing one to later road network easier to maintain
    private boolean hasAnyRoad() {
        for (int x = 0; x < gameMap.getWidth(); ++x) {
            for (int y = 0; y < gameMap.getHeight(); ++y) {
                Tile t = gameMap.getTile(new GridPos(x, y));
                if (t.getRoadPiece() != null) return true;
            }
        }
        return false;
    }

    // step 3: can we build the road?
    // limitations: not on existing road/ infrastructure/ terrain limit(not on water) / first road is ok
    private boolean canPlaceRoad(Tile tile) {
        if (tile.getRoadPiece()!=null) return false;
        if(tile.getEntity()!=null) return false;
        if (!tile.getTerrain().isPassable()) return false;
        if(!hasAnyRoad()) return true;

        // if we already has road on map, check connectivity for new one and existing roads
        boolean hasNeighborRoad = false;
        for (Tile n : getNeighbors(tile)) {
            if (n.getRoadPiece() != null) {
                hasNeighborRoad = true;
                break;
            }
        }
        return hasNeighborRoad;
    }

    // step 4: now its ok to build the road
    public void buildRoad(Tile tile) {
        if (!canPlaceRoad(tile)) return;

        RoadPiece piece = new RoadPiece(RoadKind.ROAD, null);
        piece.addTile(tile);
        tile.setRoadPiece(piece);

        roadNetwork.rebuild();
    }

    // road removal
    public void removeRoad(Tile tile) {
        tile.setRoadPiece(null);
        roadNetwork.rebuild();
    }
    //-------------------------------

    //--------- Stop placement rules ----------------

    // step 1: is the tile empty?
    private boolean isTileEmptyForStop(Tile tile) {
        if (!tile.getTerrain().isPassable()) return false;
        if (tile.getEntity() != null) return false;
        if (tile.getRoadPiece() != null) return false;
        if (tile.getStop() != null) return false;
        if (tile.getGarage() != null) return false;
        return true;
    }

    // step 2: Stop should along road
    private boolean hasAdjacentRoad(Tile tile) {
        for (Tile n : getNeighbors(tile)) {
            if (n.getRoadPiece() != null) return true;
        }
        return false;
    }

    // step 3: Stop must be near a City or Facility (servedPlace)
    private MapEntity findNearbyServedPlace(Tile tile) {
        for (Tile n : getNeighbors(tile)) {
            MapEntity e = n.getEntity();
            if (e instanceof City || e instanceof Facility) {
                return e;
            }
        }
        return null;
    }

    // step 4: Can we place a stop here? combine previous 3 conditions
    public boolean canPlaceStop(Tile tile) {
        if (!isTileEmptyForStop(tile)) return false;
        if (!hasAdjacentRoad(tile)) return false;
        if (findNearbyServedPlace(tile) == null) return false;
        return true;
    }

    // step 5: place the stop
    public Stop placeStop(Tile tile) {
        if (!canPlaceStop(tile)) return null;

        MapEntity served = findNearbyServedPlace(tile);
        Stop stop = new Stop(Id.genNew(), tile, served);

        tile.setStop(stop);
        served.attachStop(stop);

        return stop;
    }

    // stop removal
    public void removeStop(Tile tile) {
        Stop s = tile.getStop();
        if (s == null) return;

        s.getServedPlace().detachStop(s);
        tile.setStop(null);
    }


}
