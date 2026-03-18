/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */

import javafx.scene.canvas.GraphicsContext;
import model.GameMap;
import model.Tile;

public class Renderer {

    private TileRenderer tileRenderer = new TileRenderer();
    private EntityRenderer entityRenderer = new EntityRenderer();
    private VehicleRenderer vehicleRenderer = new VehicleRenderer();
    private AnimationEngine anim = new AnimationEngine();

    public void render(GraphicsContext gc, GameMap map, Camera camera) {

        //anim.update(0.016);

        for (int x = 0; x < camera.getViewportW(); x++) {
            for (int y = 0; y < camera.getViewportH(); y++) {

                int mapX = camera.getTopLeftTile().x + x;
                int mapY = camera.getTopLeftTile().y + y;

                Tile tile = map.getTile(new common.GridPos(mapX, mapY));
                if (tile != null) {
                    tileRenderer.drawTile(gc, tile, x, y);
                    entityRenderer.draw(gc, tile, x, y);
                }
            }
        }

        vehicleRenderer.drawAll(gc, map);
    }
}