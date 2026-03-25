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
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Bus;
import model.Truck;
import model.Vehicle;

import java.util.List;

public class VehicleRenderer {

    public void draw(GraphicsContext gc, List<Vehicle> vehicles, Camera camera) {
        if (vehicles == null || vehicles.isEmpty()) {
            return;
        }

        // Draw vehicles from their simulation position in world space.
        int tileSize = camera.getTileSize();
        double size = tileSize * 0.45;

        for (Vehicle vehicle : vehicles) {
            if (vehicle == null) {
                continue;
            }

            Vec2 worldPos = vehicle.getWorldPos();
            if (worldPos == null) {
                GridPos tilePos = vehicle.getTilePos();
                if (tilePos == null) {
                    continue;
                }
                worldPos = new Vec2(tilePos.x + 0.5, tilePos.y + 0.5);
            }

            Vec2 screenPos = worldToScreen(worldPos, camera);
            double drawX = screenPos.x - (size / 2.0);
            double drawY = screenPos.y - (size / 2.0);

            gc.setFill(getVehicleColor(vehicle));
            gc.fillOval(drawX, drawY, size, size);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(drawX, drawY, size, size);
        }
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
}
