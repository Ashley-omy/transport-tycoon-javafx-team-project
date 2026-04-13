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

            for (GridPos p : getFourNeighbors(pos)) {
                if (roadTiles.contains(p)) {
                    neighbors.add(p);
                }
            }
            adjacency.put(pos, neighbors);
        }
    }

    // Shared 4-direction helper used by BFS and road-access lookups.
    private List<GridPos> getFourNeighbors(GridPos pos) {
        return List.of(pos.add(1,0), pos.add(-1,0), pos.add(0, 1), pos.add(0, -1));
    }

    public boolean isConnected(Tile a, Tile b) {
        if (a == null || b == null) {
            return false;
        }
        // Connectivity check is now delegated to the shared pathfinder.
        return !findPath(a.getPos(), b.getPos()).isEmpty();
    }

    // Shared BFS pathfinder used by route validation and vehicle movement.
    // This keeps road traversal logic in one place instead of duplicating BFS in Vehicle.
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

    // Return road tiles that can be used to enter/exit a stop, garage, or road position.
    public List<GridPos> getRoadAccessTiles(GameMap map, GridPos pos) {
        if (map == null || pos == null) {
            return List.of();
        }

        List<GridPos> accessTiles = new ArrayList<>();
        if (roadTiles.contains(pos)) {
            accessTiles.add(pos);
        }

        for (GridPos neighbor : getFourNeighbors(pos)) {
            if (!map.inBounds(neighbor) || !roadTiles.contains(neighbor)) {
                continue;
            }
            appendIfDifferent(accessTiles, neighbor);
        }
        return accessTiles;
    }

    // Build full path between two non-road locations via nearest road-access candidates.
    public List<GridPos> findPathBetweenLocations(GameMap map, GridPos fromPos, GridPos toPos) {
        if (map == null || fromPos == null || toPos == null) {
            return List.of();
        }

        List<GridPos> startRoadTiles = getRoadAccessTiles(map, fromPos);
        List<GridPos> endRoadTiles = getRoadAccessTiles(map, toPos);

        List<GridPos> bestRoadPath = List.of();
        for (GridPos startRoad : startRoadTiles) {
            for (GridPos endRoad : endRoadTiles) {
                List<GridPos> roadPath = findPath(startRoad, endRoad);
                if (roadPath.isEmpty()) {
                    continue;
                }
                if (bestRoadPath.isEmpty() || roadPath.size() < bestRoadPath.size()) {
                    bestRoadPath = roadPath;
                }
            }
        }

        if (bestRoadPath.isEmpty()) {
            return List.of();
        }

        List<GridPos> fullPath = new ArrayList<>();
        fullPath.add(fromPos);
        for (GridPos roadPos : bestRoadPath) {
            appendIfDifferent(fullPath, roadPos);
        }
        appendIfDifferent(fullPath, toPos);
        return fullPath;
    }

    // Keep generated paths clean by skipping duplicate consecutive points.
    private void appendIfDifferent(List<GridPos> path, GridPos pos) {
        if (path.isEmpty() || !path.get(path.size() - 1).equals(pos)) {
            path.add(pos);
        }
    }

    private List<GridPos> reconstructPath(Map<GridPos, GridPos> previous, GridPos target) {
        // Walk backward from target to start using the predecessor map built by BFS.
        List<GridPos> path = new ArrayList<>();
        GridPos current = target;

        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        return path;
    }
}
