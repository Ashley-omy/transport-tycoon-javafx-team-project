/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import common.GridPos;
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
        if (!tile.getTerrain().isPassableForRoad()) return false;
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
}
