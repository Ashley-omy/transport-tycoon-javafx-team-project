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

import javafx.animation.AnimationTimer;
import model.Game;
import view.GameWindow;

import java.util.List;

public class GameController {

    private Game game;
    private GameWindow window;

    private InputController input;
    private SelectionController selection;
    private TimeController time; // assuming teammate implements
    private BuildController build; // teammate
    private FleetController fleet; // teammate

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
        //double scaledDelta = deltaTime * time.getSpeedMultiplier();

        // 1. Handle input
        List<InputEvent> events = input.poll();
        handleInput(events);

        // 2. Update game logic
        game.update(deltaTime);
        /*
        if (!time.isPaused()) {
            game.update(scaledDelta);
        }
        */
        // 3. Sync UI state
        window.getUIState().syncFromSelection(selection);

        // 4. Trigger render
        window.render();
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

                break;
            case STOP:

                break;
            case GARAGE:

                break;
            default:
                break;
        }
    }

    private void handleKey(InputEvent e) {
        if ("SPACE".equals(e.key)) {
            //time.togglePause();
        }
    }
}
