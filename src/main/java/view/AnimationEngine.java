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

import java.util.HashMap;
import java.util.Map;

public class AnimationEngine {
    private static final double POSITION_SMOOTHING_ALPHA = 0.45;
    private static final double ROTATION_SMOOTHING_ALPHA = 0.35;
    private static final double SNAP_DISTANCE_TILES = 1.5;
    private static final double LANE_OFFSET_TILES = 0.18;
    private static final double EPSILON = 1e-6;

    // Shared in-game clock for HUD and animation-related timing.
    private double gameTime;
    private final Map<String, Vec2> smoothedPositions = new HashMap<>();
    private final Map<String, Double> smoothedRotationDegrees = new HashMap<>();
    private final Map<String, GridPos> lastDirectionVectors = new HashMap<>();

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
        Vec2 target = interpolateTileCenters(from, to, progress);
        String vehicleKey = vehicle.getId().toString();
        Vec2 previous = smoothedPositions.get(vehicleKey);
        Vec2 smoothed = smoothPosition(previous, target);
        smoothedPositions.put(vehicleKey, smoothed);
        updateVehicleRenderRotation(vehicle);
        return smoothed;
    }

    public double getVehicleRenderRotationDegrees(Vehicle vehicle) {
        if (vehicle == null) {
            return 0.0;
        }
        return smoothedRotationDegrees.getOrDefault(
                vehicle.getId().toString(),
                getPathDirectionRotation(vehicle.getCurrentPathTile(), vehicle.getNextPathTile())
        );
    }

    private Vec2 smoothPosition(Vec2 previous, Vec2 target) {
        if (target == null) {
            return null;
        }
        if (previous == null) {
            return target;
        }

        double dx = target.x - previous.x;
        double dy = target.y - previous.y;
        double distance = Math.hypot(dx, dy);
        if (distance > SNAP_DISTANCE_TILES) {
            return target;
        }

        double x = previous.x + dx * POSITION_SMOOTHING_ALPHA;
        double y = previous.y + dy * POSITION_SMOOTHING_ALPHA;
        return new Vec2(x, y);
    }

    private void updateVehicleRenderRotation(Vehicle vehicle) {
        if (vehicle == null) {
            return;
        }

        String vehicleKey = vehicle.getId().toString();
        GridPos currentDir = getDirectionVector(vehicle.getCurrentPathTile(), vehicle.getNextPathTile());
        GridPos previousDir = lastDirectionVectors.get(vehicleKey);
        double baseHeading = getPathDirectionRotation(vehicle.getCurrentPathTile(), vehicle.getNextPathTile());
        double current = smoothedRotationDegrees.getOrDefault(vehicleKey, baseHeading);
        double target = baseHeading;

        if (isZeroDirection(currentDir)) {
            target = current;
        } else if (previousDir != null && !isZeroDirection(previousDir)) {
            if (isOrthogonalTurn(previousDir, currentDir)) {
                // Right turn: +90, Left turn: -90
                target = current + (isRightTurn(previousDir, currentDir) ? 90.0 : -90.0);
            } else {
                target = baseHeading;
            }
        }

        double blended = blendAngle(current, target, ROTATION_SMOOTHING_ALPHA);
        smoothedRotationDegrees.put(vehicleKey, normalizeAngle(blended));
        lastDirectionVectors.put(vehicleKey, currentDir);
    }

    private double getPathDirectionRotation(GridPos from, GridPos to) {
        if (from == null || to == null) {
            return 0.0;
        }

        int dirX = Integer.signum(to.x - from.x);
        int dirY = Integer.signum(to.y - from.y);
        if (dirX > 0) return 90.0;
        if (dirX < 0) return -90.0;
        if (dirY > 0) return 180.0;
        if (dirY < 0) return 0.0;
        return 0.0;
    }

    private GridPos getDirectionVector(GridPos from, GridPos to) {
        if (from == null || to == null) {
            return new GridPos(0, 0);
        }
        return new GridPos(
                Integer.signum(to.x - from.x),
                Integer.signum(to.y - from.y)
        );
    }

    private boolean isZeroDirection(GridPos dir) {
        return dir == null || (dir.x == 0 && dir.y == 0);
    }

    private boolean isOrthogonalTurn(GridPos previousDir, GridPos currentDir) {
        if (isZeroDirection(previousDir) || isZeroDirection(currentDir)) {
            return false;
        }
        int dot = previousDir.x * currentDir.x + previousDir.y * currentDir.y;
        return dot == 0;
    }

    private boolean isRightTurn(GridPos previousDir, GridPos currentDir) {
        // Screen coords (x right, y down): cross > 0 means clockwise(right) turn.
        int cross = previousDir.x * currentDir.y - previousDir.y * currentDir.x;
        return cross > 0;
    }

    private double blendAngle(double current, double target, double alpha) {
        double delta = normalizeAngle(target - current);
        return current + (delta * alpha);
    }

    private double normalizeAngle(double degrees) {
        double normalized = degrees % 360.0;
        if (normalized <= -180.0) {
            normalized += 360.0;
        }
        if (normalized > 180.0) {
            normalized -= 360.0;
        }
        if (Math.abs(normalized + 180.0) < EPSILON) {
            return 180.0;
        }
        return normalized;
    }

    private Vec2 interpolateTileCenters(GridPos from, GridPos to, double progress) {
        // Interpolate between tile centers and shift to side lane by travel direction.
        double fromX = from.x + 0.5;
        double fromY = from.y + 0.5;
        double toX = to.x + 0.5;
        double toY = to.y + 0.5;

        Vec2 laneOffset = computeLaneOffset(from, to);
        fromX += laneOffset.x;
        fromY += laneOffset.y;
        toX += laneOffset.x;
        toY += laneOffset.y;

        double x = fromX + (toX - fromX) * progress;
        double y = fromY + (toY - fromY) * progress;
        return new Vec2(x, y);
    }

    private Vec2 computeLaneOffset(GridPos from, GridPos to) {
        if (from == null || to == null) {
            return new Vec2(0.0, 0.0);
        }
        int dirX = Integer.signum(to.x - from.x);
        int dirY = Integer.signum(to.y - from.y);
        if (dirX == 0 && dirY == 0) {
            return new Vec2(0.0, 0.0);
        }

        // +X -> lower side, -X -> upper side, +Y -> left side, -Y -> right side
        return new Vec2(-dirY * LANE_OFFSET_TILES, dirX * LANE_OFFSET_TILES);
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
