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
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.*;
import java.util.ArrayList;
import java.util.List;

public class GameWindow extends BorderPane {
    // Temporary debug UI limit for the right-side event list.
    private static final int MAX_DEBUG_LINES = 14;

    // --- View ---
    private final MapView mapView;
    private final HUDView hudView;
    // Temporary debug UI shown on the right side.
    private final VBox debugEventList;
    // Temporary debug UI cache for recent event labels.
    private final List<Label> debugLabels;
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
        timeController = new TimeController();
        this.hudView = new HUDView(uiState, timeController);
        this.debugEventList = new VBox(6);
        this.debugLabels = new ArrayList<>();

        // Temporary debug panel for transport events and revenue checks.
        Label debugTitle = new Label("Debug Events");
        debugTitle.setTextFill(Color.WHITE);
        ScrollPane debugPane = new ScrollPane(debugEventList);
        debugPane.setFitToWidth(true);
        debugPane.setPrefHeight(700);
        VBox debugContainer = new VBox(8, debugTitle, debugPane);
        debugContainer.setPadding(new Insets(12));
        debugContainer.setPrefWidth(320);
        debugContainer.setMinWidth(260);
        debugContainer.setStyle("-fx-background-color: #1d1f24;");

        // Layout
        StackPane center = new StackPane(mapView);
        this.setCenter(center);
        this.setTop(hudView);
        this.setRight(debugContainer);

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
        // Temporary debug rendering path for event messages.
        for (String event : world.drainDebugMessages()) {
            appendDebugMessage(event);
        }
        hudView.render(
                company.getEconomy().getCash(),
                animationEngine.getFormattedTime(),
                timeController.getSpeed()
        );
    }

    // Temporary debug UI helper. Safe to remove with the rest of the debug panel.
    private void appendDebugMessage(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        Label label = new Label(world.stripDebugPrefix(message));
        label.setWrapText(true);
        if (world.isRevenueMessage(message)) {
            label.setTextFill(Color.LIMEGREEN);
        } else if (world.isCostMessage(message)) {
            label.setTextFill(Color.INDIANRED);
        } else {
            label.setTextFill(Color.WHITESMOKE);
        }

        debugLabels.add(0, label);
        while (debugLabels.size() > MAX_DEBUG_LINES) {
            debugLabels.remove(debugLabels.size() - 1);
        }

        debugEventList.getChildren().setAll(debugLabels);
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
