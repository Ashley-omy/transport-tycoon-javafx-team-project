/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */
import controller.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyEvent;
import model.*;

public class GameWindow extends BorderPane {

    // --- View ---
    private final MapView mapView;
    private final HUDView hudView;
    private final UIState uiState;

    // --- Controllers ---
    private final InputController inputController;
    private final SelectionController selectionController;
    private final GameController gameController;
    private final World world;
    private final Company company;
    private final TimeController timeController;
    private final AnimationEngine animationEngine;

    public GameWindow(Game game, World world, Company company) {
        this.world = world;
        this.company = company;
        this.animationEngine = new AnimationEngine();
        // -----------------------------
        // UI State
        // -----------------------------
        this.uiState = new UIState();

        // -----------------------------
        // Views
        // -----------------------------
        this.mapView = new MapView(1000, 700);
        this.hudView = new HUDView(uiState);

        // Layout
        StackPane center = new StackPane(mapView);
        this.setCenter(center);
        this.setTop(hudView);

        // -----------------------------
        // Connect Model → View
        // -----------------------------
        GameMap map = game.getWorld().getMap();
        RoadNetwork roadNetwork = game.getWorld().getRoadNetwork();
        mapView.setMap(map);
        mapView.setCompany(company);
        mapView.setUIState(uiState);

        // -----------------------------
        // Controllers
        // -----------------------------
        this.inputController = new InputController();
        this.selectionController = new SelectionController();

        // Other controllers
        timeController = new TimeController();
        BuildController buildController = new BuildController(game.getWorld(), game.getCompany());
        FleetController fleetController = new FleetController(game.getCompany(), game.getWorld());

        // Main controller
        this.gameController = new GameController(
                game,
                this,
                inputController,
                selectionController,
                timeController,
                buildController,
                fleetController
        );

        // -----------------------------
        // Input binding (JavaFX → Controller)
        // -----------------------------
        setupInput();

        // -----------------------------
        // Start game loop
        // -----------------------------
        gameController.start();
    }


    private void setupInput() {

        // Mouse events
        mapView.setOnMousePressed(inputController::onMousePressed);
        mapView.setOnMouseReleased(inputController::onMouseReleased);
        mapView.setOnMouseDragged(inputController::onMouseDragged);

        // Keyboard events
        this.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            try {
                // Always forward to InputController from the root capture phase.
                // This avoids reliance on node focus, since HUD buttons steal focus.
                inputController.onKeyPressed(e);

                // Prevent further handling that might duplicate or get swallowed by focused buttons.
                e.consume();
            } catch (Exception ignored) {
            }
        });

        // Enable focus (important for keyboard input)
        this.setFocusTraversable(true);
    }

    // -----------------------------
    // Called by GameController every frame
    // -----------------------------
    public void render() {

        mapView.render();
        hudView.render(
                company.getEconomy().getCash(),
                animationEngine.getFormattedTime(),
                timeController.getSpeedMultiplier()
        );
    }

    // -----------------------------
    // Getters (used by Controller)
    // -----------------------------
    public MapView getMapView() {
        return mapView;
    }

    public UIState getUIState() {
        return uiState;
    }

    public AnimationEngine getAnimationEngine(){return animationEngine;}

    public HUDView getHudView(){return hudView;}
}
