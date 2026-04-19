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
import model.GameMap;
import model.Tile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TileRenderer {
    private static final String LAND_TEXTURE_PATH = "/textures/land-tile.png";
    private static final String FOREST1_TEXTURE_PATH = "/textures/Forest1.png";
    private static final String FOREST2_TEXTURE_PATH = "/textures/Forest2.png";
    private static final String ROAD_HORIZONTAL_TEXTURE_PATH = "/textures/roads/horizontal.png";
    private static final String ROAD_VERTICAL_TEXTURE_PATH = "/textures/roads/vertical.png";
    private static final String ROAD_INTERSECTION_TEXTURE_PATH = "/textures/roads/intersection.png";
    private static final Image LAND_TILE_TEXTURE = loadTexture(LAND_TEXTURE_PATH);
    private static final Image FOREST1_TEXTURE = loadTexture(FOREST1_TEXTURE_PATH);
    private static final Image FOREST2_TEXTURE = loadTexture(FOREST2_TEXTURE_PATH);
    private static final Image ROAD_HORIZONTAL_TEXTURE = loadTexture(ROAD_HORIZONTAL_TEXTURE_PATH);
    private static final Image ROAD_VERTICAL_TEXTURE = loadTexture(ROAD_VERTICAL_TEXTURE_PATH);
    private static final Image ROAD_INTERSECTION_TEXTURE = loadTexture(ROAD_INTERSECTION_TEXTURE_PATH);

    public void drawTile(GraphicsContext gc, GameMap map, Tile t, Vec2 pos, int size) {
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
            Image roadTexture = resolveRoadTexture(map, t);
            if (roadTexture != null && !roadTexture.isError()) {
                gc.drawImage(roadTexture, pos.x, pos.y, size, size);
            } else {
                gc.setFill(Color.DARKGRAY);
                gc.fillRect(pos.x, pos.y, size, size);
            }
        }
        // Grid border
        gc.setStroke(t.isWater() ? Color.LIGHTBLUE : Color.DARKGREEN);
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

    private Image resolveRoadTexture(GameMap map, Tile tile) {
        if (map == null || tile == null || tile.getPos() == null || tile.getRoadPiece() == null) {
            return null;
        }

        int x = tile.getPos().x;
        int y = tile.getPos().y;

        boolean left = hasRoadAt(map, x - 1, y);
        boolean right = hasRoadAt(map, x + 1, y);
        boolean up = hasRoadAt(map, x, y - 1);
        boolean down = hasRoadAt(map, x, y + 1);

        if (left && right && up && down) {
            return ROAD_INTERSECTION_TEXTURE;
        }
        if (left || right) {
            return ROAD_HORIZONTAL_TEXTURE;
        }
        if (up || down) {
            return ROAD_VERTICAL_TEXTURE;
        }
        return null;
    }

    private boolean hasRoadAt(GameMap map, int x, int y) {
        Tile neighbor = map.getTile(new common.GridPos(x, y));
        return neighbor != null && neighbor.getRoadPiece() != null;
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
