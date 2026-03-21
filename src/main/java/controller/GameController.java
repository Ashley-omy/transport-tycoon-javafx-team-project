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
import javafx.animation.AnimationTimer;
import model.Game;
import view.GameWindow;
import model.*;

import java.util.List;

public class GameController {

    private Game game;
    private GameWindow window;

    private InputController input;
    private SelectionController selection;
    private TimeController time;
    private BuildController build;
    private FleetController fleet;

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

        // 2. Update game logic
        game.update(deltaTime);

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

        // Convert screen → tile
        var tile = window.getMapView()
                .screenToTile(e.mousePos);

        selection.selectTile(tile);

        /* Build Controller and Fleet Controller triggering logic here */
        switch (window.getUIState().getBuildMode()) {
            case ROAD:
                if (selection.getSelectedTile() != null) {
                    GridPos pos = selection.getSelectedTile();
                    build.buildRoad(pos);
                }
                break;
            case STOP:
                if (selection.getSelectedTile() != null) {
                    GridPos pos = selection.getSelectedTile();
                    build.buildStop(pos);
                }
                break;
            case GARAGE:
                break;
            default:
                break;
        }
    }

    private void handleKey(InputEvent e) {
        var cam = window.getMapView().getCamera();

        if ("UP".equals(e.key)) {
            cam.pan(0, -1);
        } else if ("DOWN".equals(e.key)) {
            cam.pan(0, 1);
        } else if ("LEFT".equals(e.key)) {
            cam.pan(-1, 0);
        } else if ("RIGHT".equals(e.key)) {
            cam.pan(1, 0);
        } else if ("SPACE".equals(e.key)) {
            //time.togglePause();
        }
    }
}
