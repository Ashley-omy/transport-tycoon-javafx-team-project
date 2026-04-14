package model;

import common.GridPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadNetworkTest {

    @Test
    void connectedRoadTilesShouldBeReportedAsConnected() {
        GameMap map = new GameMap(8, 8);
        RoadNetwork roadNetwork = new RoadNetwork();

        layRoad(map, new GridPos(1, 1));
        layRoad(map, new GridPos(2, 1));
        layRoad(map, new GridPos(3, 1));
        roadNetwork.rebuild(map);

        Tile start = map.getTile(new GridPos(1, 1));
        Tile end = map.getTile(new GridPos(3, 1));

        assertTrue(roadNetwork.isConnected(start, end));
    }

    @Test
    void separatedRoadSegmentsShouldNotBeConnected() {
        GameMap map = new GameMap(8, 8);
        RoadNetwork roadNetwork = new RoadNetwork();

        layRoad(map, new GridPos(1, 1));
        layRoad(map, new GridPos(2, 1));
        layRoad(map, new GridPos(5, 1));
        layRoad(map, new GridPos(6, 1));
        roadNetwork.rebuild(map);

        Tile start = map.getTile(new GridPos(1, 1));
        Tile end = map.getTile(new GridPos(6, 1));

        assertFalse(roadNetwork.isConnected(start, end));
    }

    @Test
    void findPathShouldReturnShortestRoadPath() {
        GameMap map = new GameMap(8, 8);
        RoadNetwork roadNetwork = new RoadNetwork();

        layRoad(map, new GridPos(1, 1));
        layRoad(map, new GridPos(2, 1));
        layRoad(map, new GridPos(3, 1));
        layRoad(map, new GridPos(3, 2));
        roadNetwork.rebuild(map);

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
    void getRoadAccessTilesShouldIncludeAdjacentRoadsForNonRoadLocation() {
        GameMap map = new GameMap(8, 8);
        RoadNetwork roadNetwork = new RoadNetwork();

        layRoad(map, new GridPos(2, 1));
        layRoad(map, new GridPos(1, 2));
        roadNetwork.rebuild(map);

        List<GridPos> accessTiles = roadNetwork.getRoadAccessTiles(map, new GridPos(1, 1));

        assertEquals(2, accessTiles.size());
        assertTrue(accessTiles.contains(new GridPos(2, 1)));
        assertTrue(accessTiles.contains(new GridPos(1, 2)));
    }

    @Test
    void findPathBetweenLocationsShouldWrapRoadPathWithEndpoints() {
        GameMap map = new GameMap(8, 8);
        RoadNetwork roadNetwork = new RoadNetwork();

        layRoad(map, new GridPos(2, 1));
        layRoad(map, new GridPos(3, 1));
        layRoad(map, new GridPos(4, 1));
        roadNetwork.rebuild(map);

        List<GridPos> path = roadNetwork.findPathBetweenLocations(
                map,
                new GridPos(1, 1),
                new GridPos(5, 1)
        );

        assertIterableEquals(
                List.of(
                        new GridPos(1, 1),
                        new GridPos(2, 1),
                        new GridPos(3, 1),
                        new GridPos(4, 1),
                        new GridPos(5, 1)
                ),
                path
        );
    }

    private void layRoad(GameMap map, GridPos pos) {
        Tile tile = map.getTile(pos);
        RoadPiece piece = new RoadPiece(RoadKind.ROAD, null);
        piece.addTile(tile);
        tile.setRoadPiece(piece);
    }
}
