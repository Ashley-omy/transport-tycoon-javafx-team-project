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

    public void setTopLeft(GridPos pos) {
        this.topLeftTile = pos;
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