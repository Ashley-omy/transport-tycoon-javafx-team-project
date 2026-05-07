package model;

import common.GridPos;

import java.util.ArrayList;
import java.util.List;

public class GameMap implements java.io.Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final int width;
    private final int height;

    // #65 Implement GameMap grid structure
    private final Tile[][] tiles;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        initializeTiles();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    private void initializeTiles() {
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                tiles[x][y] = new Tile(new GridPos(x, y), new Land());
            }
        }

        WorldInitializer.initializeMapWater(this);
    }

    // #66 Implement map bounds checking
    public boolean inBounds(GridPos pos) {
        return pos.x >= 0 && pos.y >= 0 && pos.x < width && pos.y < height;
    }

    // #67 Implement tile lookup
    public Tile getTile(GridPos pos) {
        if (!inBounds(pos)) return null;
        return tiles[pos.x][pos.y];
    }

    public void setTerrain(GridPos pos, Terrain terrain) {
        if (!inBounds(pos)) {
            throw new IllegalArgumentException("Position out of bounds: " + pos);
        }
        tiles[pos.x][pos.y].setTerrain(terrain);
    }

    public List<Tile> getAllTiles() {
        List<Tile> allTiles = new ArrayList<>(width * height);
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                allTiles.add(tiles[x][y]);
            }
        }
        return allTiles;
    }
}