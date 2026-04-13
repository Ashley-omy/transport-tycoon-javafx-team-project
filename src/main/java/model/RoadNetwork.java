/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import common.*;
import java.util.*;

public class RoadNetwork {
    private final Set<GridPos> roadTiles = new HashSet<>();

    // wanna make an adjacency list: road tile -> its connected neighbors
    private final Map<GridPos, List<GridPos>> adjacency = new HashMap<>();

    public RoadNetwork() { }

    public void rebuild(GameMap map) {
        roadTiles.clear();
        adjacency.clear();

        // step 1: we collect all road tiles
        for (int x = 0; x < map.getWidth(); ++x) {
            for (int y = 0; y < map.getHeight(); ++y) {
                GridPos pos = new GridPos(x, y);
                Tile tile = map.getTile(pos);

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

    public boolean isConnected(Tile a, Tile b) {
        if (a == null || b == null) {
            return false;
        }
        return !findPath(a.getPos(), b.getPos()).isEmpty();
    }

    // Return shortest road path between two road tiles using BFS.
    public List<GridPos> findPath(GridPos start, GridPos target) {
        if (start == null || target == null) {
            return List.of();
        }
        if (!roadTiles.contains(start) || !roadTiles.contains(target)) {
            return List.of();
        }
        if (start.equals(target)) {
            return List.of(start);
        }

        Queue<GridPos> queue = new ArrayDeque<>();
        Map<GridPos, GridPos> previous = new HashMap<>();
        queue.add(start);
        previous.put(start, null);

        while (!queue.isEmpty()) {
            GridPos current = queue.poll();

            if (current.equals(target)) {
                return reconstructPath(previous, target);
            }

            for (GridPos next : adjacency.getOrDefault(current, List.of())) {
                if (previous.containsKey(next)) {
                    continue;
                }
                previous.put(next, current);
                queue.add(next);
            }
        }

        return List.of();
    }

    private List<GridPos> reconstructPath(Map<GridPos, GridPos> previous, GridPos target) {
        List<GridPos> path = new ArrayList<>();
        GridPos current = target;

        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        return path;
    }
}
