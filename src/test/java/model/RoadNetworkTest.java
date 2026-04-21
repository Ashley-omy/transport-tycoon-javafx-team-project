package model;

import common.GridPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadNetworkTest {

    @Test
    // requirement coverage:
    // happy path: BFS finds the shortest legal road path in a simple connected road shape
    void findPathShouldReturnShortestRoadPath() {
        // step 1: build one small connected road shape
        GameMap map = new GameMap(8, 8);
        RoadNetwork roadNetwork = new RoadNetwork();

        layRoad(map, new GridPos(1, 1));
        layRoad(map, new GridPos(2, 1));
        layRoad(map, new GridPos(3, 1));
        layRoad(map, new GridPos(3, 2));
        roadNetwork.rebuild(map);

        // step 2: now BFS should return shortest road path
        List<GridPos> path = roadNetwork.findPath(new GridPos(1, 1), new GridPos(3, 2));

        assertIterableEquals(
                List.of(
                        new GridPos(1, 1),
                        new GridPos(2, 1),
                        new GridPos(3, 1),
                        new GridPos(3, 2)
                ),
                path
        );
    }

    @Test
    // non-trivial test case
    // requirement coverage:
    // difficult-to-reproduce situation: BFS must follow the long connected route in a U-shaped road network
    void findPathShouldTakeTheLongWayAroundInUShapedRoadNetwork() {
        // step 1: build one U-shaped road
        GameMap map = new GameMap(8, 8);
        RoadNetwork roadNetwork = new RoadNetwork();

        layRoad(map, new GridPos(1, 1));
        layRoad(map, new GridPos(1, 2));
        layRoad(map, new GridPos(1, 3));
        layRoad(map, new GridPos(2, 3));
        layRoad(map, new GridPos(3, 3));
        layRoad(map, new GridPos(3, 2));
        layRoad(map, new GridPos(3, 1));
        roadNetwork.rebuild(map);

        // step 2: BFS should go around the U road, not jump empty tiles
        List<GridPos> path = roadNetwork.findPath(new GridPos(1, 1), new GridPos(3, 1));

        assertIterableEquals(
                List.of(
                        new GridPos(1, 1),
                        new GridPos(1, 2),
                        new GridPos(1, 3),
                        new GridPos(2, 3),
                        new GridPos(3, 3),
                        new GridPos(3, 2),
                        new GridPos(3, 1)
                ),
                path
        );
    }

    @Test
    // requirement coverage:
    // error path: disconnected road islands should not crash the search
    // edge case: pathfinder returns empty list when no valid connection exists
    void findPathShouldReturnEmptyListForDisconnectedRoadIsland() {
        // step 1: build two disconnected road islands
        GameMap map = new GameMap(8, 8);
        RoadNetwork roadNetwork = new RoadNetwork();

        layRoad(map, new GridPos(1, 1));
        layRoad(map, new GridPos(2, 1));
        layRoad(map, new GridPos(5, 5));
        roadNetwork.rebuild(map);

        // step 2: if no connection, path should be empty list
        List<GridPos> path = roadNetwork.findPath(new GridPos(1, 1), new GridPos(5, 5));

        assertTrue(path.isEmpty());
    }

    // helper for placing simple road tile in map
    private void layRoad(GameMap map, GridPos pos) {
        Tile tile = map.getTile(pos);
        RoadPiece piece = new RoadPiece(RoadKind.ROAD, null);
        piece.addTile(tile);
        tile.setRoadPiece(piece);
    }
}
