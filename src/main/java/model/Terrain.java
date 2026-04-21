package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Terrain {
    private final List<Tile> occupiedTiles;

    protected Terrain() {
        this.occupiedTiles = new ArrayList<>();
    }

    public List<Tile> getOccupiedTiles() {
        return Collections.unmodifiableList(occupiedTiles);
    }

    public void addOccupiedTile(Tile tile) {
        if (tile != null && !occupiedTiles.contains(tile)) {
            occupiedTiles.add(tile);
        }
    }

    public void removeOccupiedTile(Tile tile) {
        occupiedTiles.remove(tile);
    }

    public abstract boolean isPassable();

    public abstract double buildMultiplier();

    public boolean isWater() {
        return false;
    }

    public boolean isLand() {
        return false;
    }

    public boolean isForest() {
        return false;
    }
}
