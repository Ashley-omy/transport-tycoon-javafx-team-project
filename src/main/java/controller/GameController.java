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
import view.GameWindow;
import model.*;
import view.BuildMode.*;

import java.util.ArrayList;
import java.util.List;

public class GameController {

    private Game game;
    private GameWindow window;

    private InputController input;
    private SelectionController selection;
    private TimeController time;
    private BuildController build;
    private FleetController fleet;
    private final List<Stop> pendingRouteStops = new ArrayList<>();

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
                    handleMouseClick(e);
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

        /* Logic to trigger Build Controller and Fleet Controller */
        switch (window.getUIState().getBuildMode()) {
            case ROAD:
                pendingRouteStops.clear();
                if (selection.getSelectedTile() != null) {
                    GridPos pos = selection.getSelectedTile();
                    result = build.buildRoad(pos);
                    window.getHudView().displayBuildResult(result);
                }
                break;
            case DECONSTRUCT:
                pendingRouteStops.clear();
                if (selection.getSelectedTile() != null) {
                    GridPos pos = selection.getSelectedTile();
                    result = build.removeRoad(pos);
                    window.getHudView().displayBuildResult(result);
                }
                break;
            case STOP:
                pendingRouteStops.clear();
                if (selection.getSelectedTile() != null) {
                    GridPos pos = selection.getSelectedTile();
                     result = build.buildStop(pos);
                     window.getHudView().displayBuildResult(result);
                }
                break;
            case GARAGE:
                pendingRouteStops.clear();
                if (selection.getSelectedTile() != null) {
                    GridPos pos = selection.getSelectedTile();
                    result = build.buildGarage(pos);
                    window.getHudView().displayBuildResult(result);
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

    private void collectRouteStop() {
        GridPos pos = selection.getSelectedTile();
        if (pos == null) {
            return;
        }

        // In route mode, each clicked stop is appended to the pending route list.
        Tile tile = game.getWorld().getMap().getTile(pos);
        Stop selectedStop = tile.getStop();
        if (selectedStop == null) {
            window.getHudView().displayBuildResult(ActionResult.fail("Select a stop to add it to the route"));
            return;
        }

        if (pendingRouteStops.contains(selectedStop)) {
            window.getHudView().displayBuildResult(ActionResult.fail("That stop is already selected"));
            return;
        }

        pendingRouteStops.add(selectedStop);
        window.getHudView().displayBuildResult(
                ActionResult.success("Selected " + pendingRouteStops.size() + " stop(s). Press Place Route to create the route.")
        );
    }

    private void handlePendingRoutePlacement() {
        if (!window.getUIState().consumeRoutePlacementRequest()) {
            return;
        }
        // Finalize the route only after the Place Route button is pressed again.
        ActionResult result = fleet.createRouteWithVehicle(pendingRouteStops);
        if (result.isSuccess()) {
            pendingRouteStops.clear();
            window.getUIState().setBuildMode(BuildMode.NONE);
        }
        window.getHudView().displayBuildResult(result);
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