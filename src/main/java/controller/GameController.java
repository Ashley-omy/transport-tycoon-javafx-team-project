/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author asuna
 * implementd UI-related parts
 */

import common.GridPos;
import common.Vec2;
import javafx.animation.AnimationTimer;
import model.Game;
import view.BuildMode;
import view.GaragePane;
import view.GameWindow;
import model.*;
import view.BuildMode.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameController {

    private Game game;
    private GameWindow window;

    private InputController input;
    private SelectionController selection;
    private TimeController time;
    private BuildController build;
    private FleetController fleet;
    private final GaragePane garagePane;
    private final List<Stop> pendingRouteStops = new ArrayList<>();
    // Shared drag state used by both camera panning and drag-build interactions.
    private Vec2 lastDragMousePos;
    private double dragCarryX;
    private double dragCarryY;
    // Road-drag state to build each tile at most once during one drag gesture.
    private GridPos lastRoadDragTile;
    private final Set<GridPos> dragRoadVisitedTiles = new HashSet<>();

    private long lastTime = 0;

    public GameController(Game game, GameWindow window,
                          InputController input,
                          SelectionController selection,
                          TimeController time,
                          BuildController build,
                          FleetController fleet) {

        this.game = game;
        this.window = window;
        this.input = input;
        this.selection = selection;
        this.time = time;
        this.build = build;
        this.fleet = fleet;
        this.garagePane = new GaragePane(game.getCompany(), fleet, window.getControlPanes()::displayBuildResult);
    }

    // Start game loop
    public void start() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {

                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                update(deltaTime);
            }
        }.start();
    }

    // Main update loop
    public void update(double deltaTime) {
        // Apply time speed multiplier (Uncomment when TimeController is implemented)
        double scaledDelta = deltaTime * time.getSpeedMultiplier();

        // 1. Handle input
        List<InputEvent> events = input.poll();
        handleInput(events);
        handlePendingRoutePlacement();

        // 2. Update game logic
        if (time.getSpeed() != TimeSpeed.PAUSE) {
            game.update(scaledDelta);
        }

        // 3. Sync UI state
        window.getUIState().syncFromSelection(selection);

        // 4. Trigger render
        window.render();
        window.getAnimationEngine().update(game.getSimDelta(),time);

    }

    private void handleInput(List<InputEvent> events) {

        for (InputEvent e : events) {
            switch (e.type) {

                case "MOUSE_DOWN":
                    // Keep click behavior (selection/build) and also arm drag tracking.
                    handleMouseClick(e);
                    beginDrag(e);
                    break;

                case "MOUSE_DRAG":
                    // Depending on current mode, drag either pans camera or lays roads.
                    handleMapDrag(e);
                    break;

                case "MOUSE_UP":
                    endDrag();
                    break;

                case "KEY_DOWN":
                    handleKey(e);
                    break;
            }
        }
    }

    private void handleMouseClick(InputEvent e) {
        ActionResult result;

        // Convert screen → tile
        var tile = window.getMapView()
                .screenToTile(e.mousePos);

        selection.selectTile(tile);
        if (tile == null) {
            return;
        }

        // In neutral mode, clicking a garage tile opens the garage management pane.
        if (window.getUIState().getBuildMode() == BuildMode.NONE) {
            Tile clickedTile = game.getWorld().getMap().getTile(tile);
            if (clickedTile != null && clickedTile.getGarage() != null) {
                garagePane.showForGarage(
                        clickedTile.getGarage(),
                        window.getScene() == null ? null : window.getScene().getWindow()
                );
                return;
            }
        }

        /* Logic to trigger Build Controller */
        switch (window.getUIState().getBuildMode()) {
            case ROAD:
                pendingRouteStops.clear();
                if (selection.getSelectedTile() != null) {
                    GridPos pos = selection.getSelectedTile();
                    result = build.buildRoad(pos);
                    window.getControlPanes().displayBuildResult(result);
                }
                break;
            case DECONSTRUCT:
                pendingRouteStops.clear();
                if (selection.getSelectedTile() != null) {
                    GridPos pos = selection.getSelectedTile();
                    result = build.removeRoad(pos);
                    window.getControlPanes().displayBuildResult(result);
                }
                break;
            case STOP:
                pendingRouteStops.clear();
                if (selection.getSelectedTile() != null) {
                    GridPos pos = selection.getSelectedTile();
                     result = build.buildStop(pos);
                     window.getControlPanes().displayBuildResult(result);
                }
                break;
            case GARAGE:
                pendingRouteStops.clear();
                if (selection.getSelectedTile() != null) {
                    GridPos pos = selection.getSelectedTile();
                    result = build.buildGarage(pos);
                    window.getControlPanes().displayBuildResult(result);
                }
                break;
            case ROUTE:
                collectRouteStop();
                break;
            default:
                pendingRouteStops.clear();
                break;
        }
    }

    private void beginDrag(InputEvent e) {
        if (e == null) {
            return;
        }
        // Initialize generic drag accumulators.
        lastDragMousePos = e.mousePos;
        dragCarryX = 0.0;
        dragCarryY = 0.0;
        dragRoadVisitedTiles.clear();

        // In ROAD mode, remember drag start tile to enable contiguous placement.
        if (window.getUIState().getBuildMode() == BuildMode.ROAD && e.mousePos != null) {
            lastRoadDragTile = window.getMapView().screenToTile(e.mousePos);
            if (lastRoadDragTile != null) {
                dragRoadVisitedTiles.add(lastRoadDragTile);
            }
        } else {
            lastRoadDragTile = null;
        }
    }

    private void handleMapDrag(InputEvent e) {
        if (e == null || e.mousePos == null || lastDragMousePos == null) {
            return;
        }

        BuildMode mode = window.getUIState().getBuildMode();
        // ROAD mode: drag should build roads, not move the camera.
        if (mode == BuildMode.ROAD) {
            handleRoadBuildDrag(e);
            return;
        }
        // Other build modes intentionally ignore drag camera movement.
        if (mode != BuildMode.NONE) {
            return;
        }

        double deltaX = e.mousePos.x - lastDragMousePos.x;
        double deltaY = e.mousePos.y - lastDragMousePos.y;
        lastDragMousePos = e.mousePos;

        dragCarryX += deltaX;
        dragCarryY += deltaY;

        var camera = window.getMapView().getCamera();
        int tileSize = camera.getTileSize();
        int moveTilesX = (int) (dragCarryX / tileSize);
        int moveTilesY = (int) (dragCarryY / tileSize);

        if (moveTilesX == 0 && moveTilesY == 0) {
            return;
        }

        dragCarryX -= moveTilesX * tileSize;
        dragCarryY -= moveTilesY * tileSize;

        // Dragging the mouse right/down should move the map in the same direction.
        camera.panClamped(game.getWorld().getMap(), -moveTilesX, -moveTilesY);
    }

    private void handleRoadBuildDrag(InputEvent e) {
        GridPos currentTile = window.getMapView().screenToTile(e.mousePos);
        if (currentTile == null) {
            return;
        }
        if (lastRoadDragTile == null) {
            lastRoadDragTile = currentTile;
            dragRoadVisitedTiles.add(currentTile);
            return;
        }
        if (currentTile.equals(lastRoadDragTile)) {
            return;
        }

        GridPos cursor = lastRoadDragTile;
        int builtCount = 0;
        ActionResult lastFailure = null;

        while (!cursor.equals(currentTile)) {
            // Build along a Manhattan path from last tile toward current cursor tile.
            // Prefer X movement first, then Y when X is aligned.
            int stepX = Integer.compare(currentTile.x, cursor.x);
            int stepY = stepX == 0 ? Integer.compare(currentTile.y, cursor.y) : 0;
            cursor = cursor.add(stepX, stepY);

            // Skip tiles already processed in this drag sequence.
            if (!dragRoadVisitedTiles.add(cursor)) {
                continue;
            }

            Tile tile = game.getWorld().getMap().getTile(cursor);
            if (tile == null || tile.getRoadPiece() != null) {
                continue;
            }

            ActionResult result = build.buildRoad(cursor);
            if (result.isSuccess()) {
                builtCount++;
            } else {
                lastFailure = result;
            }
        }

        lastRoadDragTile = currentTile;
        // Show aggregate drag-build feedback instead of per-tile spam.
        if (builtCount > 0) {
            window.getControlPanes().displayBuildResult(ActionResult.success("Built " + builtCount + " road tiles"));
        } else if (lastFailure != null) {
            window.getControlPanes().displayBuildResult(lastFailure);
        }
    }

    private void endDrag() {
        // Clear all drag-related state when gesture finishes.
        lastDragMousePos = null;
        dragCarryX = 0.0;
        dragCarryY = 0.0;
        lastRoadDragTile = null;
        dragRoadVisitedTiles.clear();
    }

    private void collectRouteStop() {
        GridPos pos = selection.getSelectedTile();
        if (pos == null) {
            return;
        }

        // In route mode, each clicked stop is appended to the pending route list.
        Tile tile = game.getWorld().getMap().getTile(pos);
        Stop selectedStop = tile.getStop();
        if (selectedStop == null) {
            window.getControlPanes().displayBuildResult(ActionResult.fail("Select a stop to add it to the route"));
            return;
        }

        if (pendingRouteStops.contains(selectedStop)) {
            window.getControlPanes().displayBuildResult(ActionResult.fail("That stop is already selected"));
            return;
        }

        pendingRouteStops.add(selectedStop);
        window.getControlPanes().displayBuildResult(
                ActionResult.success("Selected " + pendingRouteStops.size() + " stop(s). Press Place Route to create the route.")
        );
    }

    private void handlePendingRoutePlacement() {
        if (!window.getUIState().consumeRoutePlacementRequest()) {
            return;
        }
        // Finalize the route only after the Place Route button is pressed again.
        ActionResult result = fleet.createRoute(pendingRouteStops);
        if (result.isSuccess()) {
            pendingRouteStops.clear();
            window.getUIState().setBuildMode(BuildMode.NONE);
        }
        window.getControlPanes().displayBuildResult(result);
    }

    private void handleKey(InputEvent e) {
        var cam = window.getMapView().getCamera();
        var map = game.getWorld().getMap();

        if ("UP".equals(e.key)) {
            cam.panClamped(map, 0, -1);
        } else if ("DOWN".equals(e.key)) {
            cam.panClamped(map, 0, 1);
        } else if ("LEFT".equals(e.key)) {
            cam.panClamped(map, -1, 0);
        } else if ("RIGHT".equals(e.key)) {
            cam.panClamped(map, 1, 0);
        } else if ("SPACE".equals(e.key)) {
            //time.togglePause();
        }
    }

    public void handleMinimapInput(double x, double y) {
        var minimap = window.getMinimapView();
        GridPos targetTopLeft = minimap.minimapToCameraTopLeft(new Vec2(x, y));
        if (targetTopLeft == null) {
            return;
        }
        window.getMapView().getCamera().setTopLeftClamped(minimap.getMap(), targetTopLeft);
    }
}
