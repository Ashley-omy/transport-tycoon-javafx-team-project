/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */
import common.Vec2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import model.City;
import model.Facility;
import model.MapEntity;
import model.Mine;
import model.Tile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EntityRenderer {
    private static final String MINE_TEXTURE_PATH = "/textures/Mine.png";
    private static final String FACILITY_TEXTURE_PATH = "/textures/Facility.png";
    private static final String GARAGE_TEXTURE_PATH = "/textures/Garage.png";

    private static final Image MINE_TEXTURE = loadTexture(MINE_TEXTURE_PATH);
    private static final Image FACILITY_TEXTURE = loadTexture(FACILITY_TEXTURE_PATH);
    private static final Image GARAGE_TEXTURE = loadTexture(GARAGE_TEXTURE_PATH);

    public void draw(GraphicsContext gc, Tile t, Vec2 pos, int size) {
        if (t.getGarage() != null) {
            drawSingleTileImageOrFallback(gc, GARAGE_TEXTURE, Color.ORANGE, pos, size);
        }

        MapEntity entity = t.getEntity();
        if (entity instanceof City city) {
            // Keep city internal road tiles visible; color only non-internal city tiles.
            boolean hideCityFillForInternalRoad = t.getRoadPiece() != null
                    && city.hasInternalRoadAt(t.getPos());
            if (!hideCityFillForInternalRoad) {
                gc.setFill(Color.DARKRED);
                gc.fillRect(pos.x, pos.y, size, size);
            }
        } else if (entity instanceof Mine mine) {
            // Mine footprint is 2x2; draw one sprite across all occupied tiles.
            drawMultiTileEntityImageIfTopLeft(gc, t, pos, size, mine, MINE_TEXTURE, Color.SADDLEBROWN);
        } else if (entity instanceof Facility facility) {
            // Facility footprint is 2x2; draw one sprite across all occupied tiles.
            drawMultiTileEntityImageIfTopLeft(gc, t, pos, size, facility, FACILITY_TEXTURE, Color.MEDIUMPURPLE);
        } else if (entity != null) {
            gc.setFill(Color.DARKRED);
            gc.fillRect(pos.x, pos.y, size, size);
        }

        if (t.getStop() != null) {
            double outerRadius = size * 0.34;
            double innerRadius = size * 0.16;
            double centerX = pos.x + (size * 0.5);
            double centerY = pos.y + (size * 0.5);

            gc.setFill(Color.RED);
            gc.fillOval(centerX - outerRadius, centerY - outerRadius,
                    outerRadius * 2, outerRadius * 2);

            gc.setFill(Color.WHITE);
            gc.fillOval(centerX - innerRadius, centerY - innerRadius,
                    innerRadius * 2, innerRadius * 2);
        }
    }

    // Draws a multi-tile sprite exactly once from the top-left tile of the entity footprint.
    private void drawMultiTileEntityImageIfTopLeft(GraphicsContext gc, Tile currentTile, Vec2 pos, int size,
                                                   MapEntity entity, Image image, Color fallbackColor) {
        if (!isTopLeftEntityTile(entity, currentTile)) {
            return;
        }

        int footprint = Math.max(1, entity.getFootprintW());
        double width = size * footprint;
        double height = size * footprint;
        if (image != null && !image.isError()) {
            gc.drawImage(image, pos.x, pos.y, width, height);
            return;
        }

        gc.setFill(fallbackColor);
        gc.fillRect(pos.x, pos.y, width, height);
    }

    private void drawSingleTileImageOrFallback(GraphicsContext gc, Image image, Color fallbackColor, Vec2 pos, int size) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, pos.x, pos.y, size, size);
            return;
        }

        gc.setFill(fallbackColor);
        gc.fillRect(pos.x, pos.y, size, size);
    }

    private boolean isTopLeftEntityTile(MapEntity entity, Tile currentTile) {
        if (entity == null || currentTile == null) {
            return false;
        }
        if (entity.getOccupiedTiles() == null || entity.getOccupiedTiles().isEmpty()) {
            return true;
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (Tile occupied : entity.getOccupiedTiles()) {
            if (occupied == null || occupied.getPos() == null) {
                continue;
            }
            minX = Math.min(minX, occupied.getPos().x);
            minY = Math.min(minY, occupied.getPos().y);
        }

        if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE) {
            return true;
        }

        return currentTile.getPos().x == minX && currentTile.getPos().y == minY;
    }

    private static Image loadTexture(String resourcePath) {
        try (InputStream stream = EntityRenderer.class.getResourceAsStream(resourcePath)) {
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
