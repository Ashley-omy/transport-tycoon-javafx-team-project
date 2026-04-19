/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 * This class renders terrain tiles
 */
import common.Vec2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import model.City;
import model.Factory;
import model.Mine;
import model.Tile;

import java.io.InputStream;

public class TileRenderer {
    private static final String LAND_TEXTURE_PATH = "/textures/land-tile.png";
    private static final Image LAND_TILE_TEXTURE = loadLandTileTexture();

    public void drawTile(GraphicsContext gc, Tile t, Vec2 pos, int size) {
        boolean baseTileAlreadyDrawn = false;

        // Simple color by terrain
        if (t.isWater()) {
            gc.setFill(Color.LIGHTBLUE);
        } else if (t.isForest()) {
            gc.setFill(Color.FORESTGREEN);
        } else {
            if (LAND_TILE_TEXTURE != null && !LAND_TILE_TEXTURE.isError()) {
                // Draw one land texture per tile.
                gc.drawImage(LAND_TILE_TEXTURE, pos.x, pos.y, size, size);
                baseTileAlreadyDrawn = true;
            } else {
                gc.setFill(Color.GREEN);
            }
        }

        if (!baseTileAlreadyDrawn) {
            gc.fillRect(pos.x, pos.y, size, size);
        }

        // Road overlay
        if (t.getRoadPiece() != null) {
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(pos.x, pos.y, size, size);
        }
        boolean hideCityFillForInternalRoad = t.getEntity() instanceof City city
                && t.getRoadPiece() != null
                && city.hasInternalRoadAt(t.getPos());

        if (t.getEntity() != null && !hideCityFillForInternalRoad) {
            if (t.getEntity() instanceof Factory) {
                gc.setFill(Color.GRAY);
            } else if (t.getEntity() instanceof Mine) {
                gc.setFill(Color.SADDLEBROWN);
            } else {
                gc.setFill(Color.ORANGE);
            }
            gc.fillRect(pos.x + size * 0.2, pos.y + size * 0.2,
                    size * 0.6, size * 0.6);
        }
        if (t.getStop() != null) {
            gc.setFill(Color.RED);
            gc.fillRect(pos.x + size * 0.2, pos.y + size * 0.2,
                    size * 0.6, size * 0.6);
        }

        // Grid border
        gc.setStroke(Color.DARKGREEN);
        gc.strokeRect(pos.x, pos.y, size, size);
    }

    private static Image loadLandTileTexture() {
        try (InputStream stream = TileRenderer.class.getResourceAsStream(LAND_TEXTURE_PATH)) {
            if (stream == null) {
                return null;
            }
            return new Image(stream);
        } catch (Exception ignored) {
            return null;
        }
    }
}
