package model;

import common.GridPos;

public class GameMap {
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
}
