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
import model.Forest;
import model.Tile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TileRenderer {
    private static final String LAND_TEXTURE_PATH = "/textures/land-tile.png";
    private static final String FOREST1_TEXTURE_PATH = "/textures/Forest1.png";
    private static final String FOREST2_TEXTURE_PATH = "/textures/Forest2.png";
    private static final Image LAND_TILE_TEXTURE = loadTexture(LAND_TEXTURE_PATH);
    private static final Image FOREST1_TEXTURE = loadTexture(FOREST1_TEXTURE_PATH);
    private static final Image FOREST2_TEXTURE = loadTexture(FOREST2_TEXTURE_PATH);

    public void drawTile(GraphicsContext gc, Tile t, Vec2 pos, int size) {
        boolean baseTileAlreadyDrawn = false;

        // Simple color by terrain
        if (t.isWater()) {
            gc.setFill(Color.LIGHTBLUE);
        } else if (t.isForest()) {
            Image forestTexture = resolveForestTexture(t);
            if (forestTexture != null && !forestTexture.isError()) {
                gc.drawImage(forestTexture, pos.x, pos.y, size, size);
                baseTileAlreadyDrawn = true;
            } else {
                gc.setFill(Color.FORESTGREEN);
            }
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
        // Grid border
        gc.setStroke(Color.DARKGREEN);
        gc.strokeRect(pos.x, pos.y, size, size);
    }

    private Image resolveForestTexture(Tile tile) {
        if (!(tile.getTerrain() instanceof Forest forest)) {
            return null;
        }

        int trees = forest.getTrees();
        if (trees <= 2) {
            return FOREST1_TEXTURE;
        }
        if (trees >= 3) {
            return FOREST2_TEXTURE;
        }
        return null;
    }

    private static Image loadTexture(String resourcePath) {
        try (InputStream stream = TileRenderer.class.getResourceAsStream(resourcePath)) {
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception ignored) {
        }
        return loadTextureFromProjectPath(resourcePath);
    }

    private static Image loadTextureFromProjectPath(String resourcePath) {
        try {
            String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            Path filePath = Paths.get("src", "main", "resources").resolve(normalized);
            if (!Files.exists(filePath)) {
                return null;
            }
            return new Image(filePath.toUri().toString());
        } catch (Exception ignored) {
            return null;
        }
    }
}
