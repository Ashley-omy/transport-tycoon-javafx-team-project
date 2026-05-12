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
import model.BridgeType;
import model.Forest;
import model.GameMap;
import model.RoadKind;
import model.RoadPiece;
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
    private static final String BRIDGE1_TEXTURE_PATH = "/textures/bridges/Bridge1.png";
    private static final String BRIDGE2_TEXTURE_PATH = "/textures/bridges/Bridge2.png";
    private static final String BRIDGE3_TEXTURE_PATH = "/textures/bridges/Bridge3.png";
    private static final Image LAND_TILE_TEXTURE = loadTexture(LAND_TEXTURE_PATH);
    private static final Image FOREST1_TEXTURE = loadTexture(FOREST1_TEXTURE_PATH);
    private static final Image FOREST2_TEXTURE = loadTexture(FOREST2_TEXTURE_PATH);
    private static final Image ROAD_HORIZONTAL_TEXTURE = loadTexture(ROAD_HORIZONTAL_TEXTURE_PATH);
    private static final Image ROAD_VERTICAL_TEXTURE = loadTexture(ROAD_VERTICAL_TEXTURE_PATH);
    private static final Image ROAD_INTERSECTION_TEXTURE = loadTexture(ROAD_INTERSECTION_TEXTURE_PATH);
    private static final Image BRIDGE1_TEXTURE = loadTexture(BRIDGE1_TEXTURE_PATH);
    private static final Image BRIDGE2_TEXTURE = loadTexture(BRIDGE2_TEXTURE_PATH);
    private static final Image BRIDGE3_TEXTURE = loadTexture(BRIDGE3_TEXTURE_PATH);

    public void drawTile(GraphicsContext gc, GameMap map, Tile t, Vec2 pos, int size) {
        boolean baseTileAlreadyDrawn = false;
        boolean bridgeTileDrawn = false;

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
            RoadPiece roadPiece = t.getRoadPiece();
            if (roadPiece.getKind() == RoadKind.BRIDGE) {
                Image bridgeTexture = resolveBridgeTexture(roadPiece);
                if (bridgeTexture != null && !bridgeTexture.isError()) {
                    drawBridgeTexture(gc, map, t, bridgeTexture, pos, size);
                } else {
                    Color bridgeColor = BridgeVisuals.colorFor(
                            roadPiece.getBridgeSpec() == null ? null : roadPiece.getBridgeSpec().getType()
                    );
                    gc.setFill(bridgeColor);
                    gc.fillRect(pos.x - 0.5, pos.y - 0.5, size + 1.0, size + 1.0);
                    gc.setStroke(bridgeColor.darker());
                    gc.setLineWidth(1.0);
                    gc.strokeRect(pos.x + 0.5, pos.y + 0.5, size - 1.0, size - 1.0);
                }
                bridgeTileDrawn = true;
            } else {
                Image roadTexture = resolveRoadTexture(map, t);
                if (roadTexture != null && !roadTexture.isError()) {
                    gc.drawImage(roadTexture, pos.x, pos.y, size, size);
                } else {
                    gc.setFill(Color.DARKGRAY);
                    gc.fillRect(pos.x, pos.y, size, size);
                }
            }
        }

        if (!bridgeTileDrawn) {
            // Grid border
            gc.setStroke(t.isWater() ? Color.LIGHTBLUE : Color.rgb(98, 107, 2));
            gc.setLineWidth(0.4);
            gc.strokeRect(pos.x, pos.y, size, size);
        }
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

    private Image resolveBridgeTexture(RoadPiece roadPiece) {
        if (roadPiece == null || roadPiece.getBridgeSpec() == null) {
            return BRIDGE1_TEXTURE;
        }
        BridgeType type = roadPiece.getBridgeSpec().getType();
        if (type == null) {
            return BRIDGE1_TEXTURE;
        }
        return switch (type) {
            case TYPE_A -> BRIDGE1_TEXTURE;
            case TYPE_B -> BRIDGE2_TEXTURE;
            case TYPE_C -> BRIDGE3_TEXTURE;
        };
    }

    private void drawBridgeTexture(GraphicsContext gc, GameMap map, Tile tile, Image texture, Vec2 pos, int size) {
        if (gc == null || texture == null) {
            return;
        }

        if (shouldRotateBridge90(map, tile)) {
            double centerX = pos.x + size / 2.0;
            double centerY = pos.y + size / 2.0;
            gc.save();
            gc.translate(centerX, centerY);
            gc.rotate(90);
            gc.drawImage(texture, -size / 2.0, -size / 2.0, size, size);
            gc.restore();
            return;
        }

        gc.drawImage(texture, pos.x, pos.y, size, size);
    }

    private boolean shouldRotateBridge90(GameMap map, Tile tile) {
        if (map == null || tile == null || tile.getPos() == null) {
            return false;
        }

        int x = tile.getPos().x;
        int y = tile.getPos().y;
        boolean up = hasBridgeAt(map, x, y - 1);
        boolean down = hasBridgeAt(map, x, y + 1);
        if (up || down) {
            return false;
        }
        boolean left = hasBridgeAt(map, x - 1, y);
        boolean right = hasBridgeAt(map, x + 1, y);
        return left || right;
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

    private boolean hasBridgeAt(GameMap map, int x, int y) {
        Tile neighbor = map.getTile(new common.GridPos(x, y));
        return neighbor != null
                && neighbor.getRoadPiece() != null
                && neighbor.getRoadPiece().getKind() == RoadKind.BRIDGE;
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
