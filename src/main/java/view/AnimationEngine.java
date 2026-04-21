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
import model.Vehicle;

public class AnimationEngine {
    // Shared in-game clock for HUD and animation-related timing.
    private double gameTime;

    public void update(double deltaTime) {
        // Accumulate already-scaled simulation delta from GameController/Game.
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) {
            return;
        }
        gameTime += deltaTime;
    }

    public String getFormattedTime() {
        int totalSeconds = (int) gameTime;

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public double getGameTime(){return gameTime;}

    public Vec2 getVehicleRenderPos(Vehicle vehicle) {
        // Convert simulation movement state (from tile, to tile, progress) into world-space
        if (vehicle == null) {
            return null;
        }

        GridPos from = vehicle.getCurrentPathTile();
        if (from == null) {
            return null;
        }

        GridPos to = vehicle.getNextPathTile();
        if (to == null) {
            to = from;
        }

        double progress = from.equals(to) ? 0.0 : clamp01(vehicle.getSegmentProgress());
        return interpolateTileCenters(from, to, progress);
    }

    private Vec2 interpolateTileCenters(GridPos from, GridPos to, double progress) {
        // Interpolate between tile centers so vehicles stay visually centered on roads.
        double fromX = from.x + 0.5;
        double fromY = from.y + 0.5;
        double toX = to.x + 0.5;
        double toY = to.y + 0.5;
        double x = fromX + (toX - fromX) * progress;
        double y = fromY + (toY - fromY) * progress;
        return new Vec2(x, y);
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
