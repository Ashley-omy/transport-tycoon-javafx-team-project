/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;
import javafx.scene.canvas.GraphicsContext;
import model.GameMap;
import model.Vehicle;

import java.util.List;
/**
 *
 * @author asuna
 */
public class Renderer {
    private final TileRenderer tileRenderer;
    private final EntityRenderer entityRenderer;
    private final VehicleRenderer vehicleRenderer;
    private final AnimationEngine anim;

    public Renderer() {
        this.tileRenderer = new TileRenderer();
        this.entityRenderer = new EntityRenderer();
        this.vehicleRenderer = new VehicleRenderer();
        this.anim = new AnimationEngine();
    }

    public void render(GraphicsContext gc, GameMap map, Camera camera, UIState uiState, List<Vehicle> vehicles) {

        int tileSize = camera.getTileSize();

        // Visible tile range
        int startX = camera.getTopLeftTile().x;
        int startY = camera.getTopLeftTile().y;

        int endX = startX + camera.getViewportW() / tileSize + 2;
        int endY = startY + camera.getViewportH() / tileSize + 2;

        // Pass 1: draw base terrain/roads for all visible tiles.
        // This prevents multi-tile entity sprites from being partially overdrawn
        // by later tile paints in the same frame.
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                var pos = new common.GridPos(x, y);
                if (!map.inBounds(pos)) continue;

                var tile = map.getTile(pos);
                var screenPos = camera.tileToScreen(pos);
                tileRenderer.drawTile(gc, tile, screenPos, tileSize);
            }
        }

        // Pass 2: draw entities/stops/garages on top of the finished base.
        // Expand the scan range so multi-tile entities whose top-left tile is slightly
        // outside the viewport can still be drawn correctly.
        int entityPadding = 5; // current max footprint size in tiles
        int entityStartX = Math.max(0, startX - entityPadding);
        int entityStartY = Math.max(0, startY - entityPadding);
        int entityEndX = Math.min(map.getWidth(), endX + 1);
        int entityEndY = Math.min(map.getHeight(), endY + 1);

        for (int y = entityStartY; y < entityEndY; y++) {
            for (int x = entityStartX; x < entityEndX; x++) {
                var pos = new common.GridPos(x, y);
                if (!map.inBounds(pos)) continue;

                var tile = map.getTile(pos);
                var screenPos = camera.tileToScreen(pos);
                entityRenderer.draw(gc, tile, screenPos, tileSize);
            }
        }

        // Vehicles (draw after tiles)
        vehicleRenderer.draw(gc, vehicles, camera);
    }
}
