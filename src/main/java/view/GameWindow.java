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
import model.Game;
import model.GameMap;
import model.RoadNetwork;

public class GameWindow extends BorderPane {

    // --- View ---
    private final MapView mapView;
    private final HUDView hudView;
    private final UIState uiState;

    // --- Controllers ---
    private final InputController inputController;
    private final SelectionController selectionController;
    private final GameController gameController;

    public GameWindow(Game game) {

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
        RoadNetwork roadNetwork = new RoadNetwork(map);
        mapView.setMap(map);
        mapView.setUIState(uiState);

        // -----------------------------
        // Controllers
        // -----------------------------
        this.inputController = new InputController();
        this.selectionController = new SelectionController();

        // Other controllers
        TimeController timeController = new TimeController();
        BuildController buildController = new BuildController(map,roadNetwork);
        FleetController fleetController = new FleetController();

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
        this.setOnKeyPressed(inputController::onKeyPressed);

        // Enable focus (important for keyboard input)
        this.setFocusTraversable(true);
    }

    // -----------------------------
    // Called by GameController every frame
    // -----------------------------
    public void render() {
        mapView.render();
        hudView.render();
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
}