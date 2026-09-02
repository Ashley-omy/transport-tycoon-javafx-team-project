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
import common.GridPos;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import model.Bus;
import model.Truck;
import model.Vehicle;
import model.VehicleState;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class VehicleRenderer {
    private static final String BIG_BUS_TEXTURE_PATH = "/assets/vehicles/BigBus.png";
    private static final String BIG_TRUCK_TEXTURE_PATH = "/assets/vehicles/BigTruck.png";
    private static final String SMALL_BUS_TEXTURE_PATH = "/assets/vehicles/SmallBus .png";
    private static final String SMALL_TRUCK_TEXTURE_PATH = "/assets/vehicles/SmallTruck .png";
    private static final Image BIG_BUS_TEXTURE = loadTexture(BIG_BUS_TEXTURE_PATH);
    private static final Image BIG_TRUCK_TEXTURE = loadTexture(BIG_TRUCK_TEXTURE_PATH);
    private static final Image SMALL_BUS_TEXTURE = loadTexture(SMALL_BUS_TEXTURE_PATH);
    private static final Image SMALL_TRUCK_TEXTURE = loadTexture(SMALL_TRUCK_TEXTURE_PATH);

    private final AnimationEngine animationEngine;

    public VehicleRenderer(AnimationEngine animationEngine) {
        if (animationEngine == null) {
            throw new IllegalArgumentException("animationEngine cannot be null");
        }
        this.animationEngine = animationEngine;
    }

    public void draw(GraphicsContext gc, List<Vehicle> vehicles, Camera camera) {
        if (vehicles == null || vehicles.isEmpty()) {
            return;
        }

        // Draw vehicles from their simulation position in world space.
        int tileSize = camera.getTileSize();
        double size = tileSize * 0.62;

        for (Vehicle vehicle : vehicles) {
            if (vehicle == null) {
                continue;
            }

            Vec2 worldPos = animationEngine.getVehicleRenderPos(vehicle);
            if (worldPos == null) continue;

            Vec2 screenPos = worldToScreen(worldPos, camera);
            double drawX = screenPos.x - (size / 2.0);
            double drawY = screenPos.y - (size / 2.0);

            Image texture = getVehicleTexture(vehicle);
            if (texture != null && !texture.isError()) {
                double rotation = animationEngine.getVehicleRenderRotationDegrees(vehicle);
                gc.save();
                gc.translate(screenPos.x, screenPos.y);
                gc.rotate(rotation);
                gc.drawImage(texture, -size / 2.0, -size / 2.0, size, size);
                gc.restore();
            } else {
                gc.setFill(getVehicleColor(vehicle));
                gc.fillOval(drawX, drawY, size, size);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(drawX, drawY, size, size);
            }
            drawStateLabelIfNeeded(gc, vehicle, screenPos, drawY);
        }
    }
    /* Temporal function for debugging */
    private void drawStateLabelIfNeeded(GraphicsContext gc, Vehicle vehicle, Vec2 screenPos, double vehicleTopY) {
        VehicleState state = vehicle.getState();
        if (state != VehicleState.IN_GARAGE && state != VehicleState.BLOCKED) {
            return;
        }

        gc.save();
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.BOTTOM);
        gc.fillText(state.name(), screenPos.x, vehicleTopY - 3.0);
        gc.restore();
    }

    private Vec2 worldToScreen(Vec2 worldPos, Camera camera) {
        // Convert tile-based world coordinates into on-screen pixel coordinates.
        int tileSize = camera.getTileSize();
        GridPos topLeftTile = camera.getTopLeftTile();
        double x = (worldPos.x - topLeftTile.x) * tileSize;
        double y = (worldPos.y - topLeftTile.y) * tileSize;
        return new Vec2(x, y);
    }

    private Color getVehicleColor(Vehicle vehicle) {
        // Keep the two vehicle types visually distinct with simple colors.
        if (vehicle instanceof Bus) {
            return Color.DODGERBLUE;
        }
        if (vehicle instanceof Truck) {
            return Color.DARKORANGE;
        }
        return Color.DARKSLATEGRAY;
    }

    private Image getVehicleTexture(Vehicle vehicle) {
        if (vehicle instanceof Bus) {
            return vehicle.getCapacityUnits() >= 80 ? BIG_BUS_TEXTURE : SMALL_BUS_TEXTURE;
        }
        if (vehicle instanceof Truck) {
            return vehicle.getCapacityUnits() >= 150 ? BIG_TRUCK_TEXTURE : SMALL_TRUCK_TEXTURE;
        }
        return null;
    }

    private static Image loadTexture(String resourcePath) {
        try (InputStream stream = VehicleRenderer.class.getResourceAsStream(resourcePath)) {
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
