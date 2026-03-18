/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author lenovo
 */
import common.GridPos;
import common.GridPos;

public class Camera {

    // Top-left tile of the current view (in grid coordinates)
    private GridPos topLeftTile = new GridPos(0, 0);

    // Size of the visible area (in number of tiles)
    private int viewportW = 20;
    private int viewportH = 15;

    // Move the camera by dx, dy (in tiles)
    // GridPos is immutable, so we create a new instance instead of modifying fields
    public void pan(int dx, int dy) {
        topLeftTile = new GridPos(
                topLeftTile.x + dx,
                topLeftTile.y + dy
        );
    }

    // Directly set camera position
    public void setTopLeft(GridPos pos) {
        this.topLeftTile = pos;
    }

    // Get current top-left tile
    public GridPos getTopLeftTile() {
        return topLeftTile;
    }

    public int getViewportW() {
        return viewportW;
    }

    public int getViewportH() {
        return viewportH;
    }
}