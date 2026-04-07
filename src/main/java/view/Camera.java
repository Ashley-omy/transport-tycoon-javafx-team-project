/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */

import common.GridPos;
import common.Vec2;
import model.GameMap;

public class Camera {

    private GridPos topLeftTile;
    private int viewportW;
    private int viewportH;

    private int tileSize = 32; // pixels per tile

    public Camera(GridPos topLeftTile, int viewportW, int viewportH) {
        this.topLeftTile = topLeftTile;
        this.viewportW = viewportW;
        this.viewportH = viewportH;
    }

    // Move camera by tile offset
    public void pan(int dx, int dy) {
        topLeftTile = new GridPos(
                topLeftTile.x + dx,
                topLeftTile.y + dy
        );
    }

    public void panClamped(GameMap map, int dx, int dy) {
        if (map == null) {
            pan(dx, dy);
            return;
        }
        setTopLeftClamped(map, topLeftTile.add(dx, dy));
    }

    public void setTopLeft(GridPos pos) {
        this.topLeftTile = pos;
    }

    public void setTopLeftClamped(GameMap map, GridPos pos) {
        if (map == null || pos == null) {
            return;
        }

        int visibleTilesX = Math.max(1, viewportW / tileSize);
        int visibleTilesY = Math.max(1, viewportH / tileSize);
        int maxX = Math.max(0, map.getWidth() - visibleTilesX);
        int maxY = Math.max(0, map.getHeight() - visibleTilesY);

        int clampedX = Math.max(0, Math.min(pos.x, maxX));
        int clampedY = Math.max(0, Math.min(pos.y, maxY));
        topLeftTile = new GridPos(clampedX, clampedY);
    }

    public GridPos getTopLeftTile() {
        return topLeftTile;
    }

    public int getViewportW() {
        return viewportW;
    }

    public int getViewportH() {
        return viewportH;
    }

    public int getTileSize() {
        return tileSize;
    }

    // Convert screen (pixels) → tile
    public GridPos screenToTile(Vec2 screenPos) {
        int tileX = (int)(screenPos.x / tileSize) + topLeftTile.x;
        int tileY = (int)(screenPos.y / tileSize) + topLeftTile.y;
        return new GridPos(tileX, tileY);
    }

    // Convert tile → screen (top-left pixel)
    public Vec2 tileToScreen(GridPos tile) {
        double x = (tile.x - topLeftTile.x) * tileSize;
        double y = (tile.y - topLeftTile.y) * tileSize;
        return new Vec2(x, y);
    }
}
