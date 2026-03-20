/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import common.*;
import java.util.*;

public class RoadNetwork {
    private final GameMap gameMap;
    private final Set<GridPos> roadTiles = new HashSet<>();

    // wanna make an adjacency list: road tile -> its connected neighbors
    private final Map<GridPos, List<GridPos>> adjacency = new HashMap<>();

    public RoadNetwork(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public void rebuild() {
        roadTiles.clear();
        adjacency.clear();

        // step 1: we collect all road tiles
        for (int x = 0; x < gameMap.getWidth(); ++x) {
            for (int y = 0; y < gameMap.getHeight(); ++y) {
                GridPos pos = new GridPos(x, y);
                Tile tile = gameMap.getTile(pos);

                if (tile.getRoadPiece() != null) {
                    roadTiles.add(pos);
                }
            }
        }

        // step 2: crate adjacency list
        for(GridPos pos : roadTiles) {
            List<GridPos> neighbors = new ArrayList<>();

            for (GridPos p : getAll4Directions(pos)) {
                if (roadTiles.contains(p)) {
                    neighbors.add(p);
                }
            }
            adjacency.put(pos, neighbors);
        }
    }

    private List<GridPos> getAll4Directions(GridPos pos) {
        return List.of(pos.add(1,0), pos.add(-1,0), pos.add(0, 1), pos.add(0, -1));
    }

    // we wonder if two tiles are connected(if its possible from a to b)
    public boolean isConnected(Tile a, Tile b) {
        GridPos start = a.getPos();
        GridPos target = b.getPos();

        // if not in road tiles then ofc not connected
        if (!roadTiles.contains(start) || !roadTiles.contains((target))) {
            return false;
        }

        // I use BFS for find the nearest path
        Queue<GridPos> queue = new LinkedList<>();
        Set<GridPos> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            GridPos current = queue.poll();

            if(current.equals((target))) {
                return true;
            }

            for (GridPos next : adjacency.getOrDefault(current, List.of())) {
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return false;
    }
}
