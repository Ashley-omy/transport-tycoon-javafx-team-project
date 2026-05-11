/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */
import controller.BuildController;
import controller.FleetController;
import controller.GameController;
import controller.InputController;
import controller.SelectionController;
import controller.TimeController;
import common.Money;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Company;
import model.Game;
import model.GameMap;
import model.World;

import java.util.function.BooleanSupplier;

public class GameWindow extends BorderPane {
    private static final int INITIAL_MAP_VIEW_WIDTH = 1000;
    private static final int INITIAL_MAP_VIEW_HEIGHT = 700;
    private static final int BUILD_PANE_TO_MAP_GAP = 16;
    private static final int HUD_TOP_MARGIN = 10;
    private static final int SPEED_PANE_TOP_MARGIN = 18;
    private static final String CENTER_BACKGROUND_STYLE = "-fx-background-color: black;";

    // --- View ---
    private final MapView mapView;
    private final HUDView hudView;
    private final MinimapView minimapView;
    private final ControlPanes controlPanes;
    private final UIState uiState;
    private final Game game;
    private final Runnable onRestartRequested;
    private final Runnable onLeaveRequested;
    private final BooleanSupplier onSaveRequested;
    private final GameOverPane gameOverOverlay;
    private boolean gameOverOverlayShown;

    // --- Controllers ---
    private final InputController inputController;
    private final SelectionController selectionController;
    private final GameController gameController;
    private final World world;
    private final Company company;
    private final TimeController timeController;
    private final AnimationEngine animationEngine;
    private Money lastRenderedCash;

    public GameWindow(Game game, World world, Company company, Runnable onRestartRequested, Runnable onLeaveRequested, BooleanSupplier onSaveRequested) {
        this.game = game;
        this.world = world;
        this.company = company;
        this.onRestartRequested = onRestartRequested == null ? () -> { } : onRestartRequested;
        this.onLeaveRequested = onLeaveRequested == null ? () -> { } : onLeaveRequested;
        this.onSaveRequested = onSaveRequested == null ? () -> false : onSaveRequested;
        this.animationEngine = new AnimationEngine();
        this.lastRenderedCash = company.getEconomy().getCash();
        this.gameOverOverlayShown = false;

        // -----------------------------
        // UI State
        // -----------------------------
        this.uiState = new UIState();

        // -----------------------------
        // Views
        // -----------------------------
        this.mapView = new MapView(INITIAL_MAP_VIEW_WIDTH, INITIAL_MAP_VIEW_HEIGHT, animationEngine);
        this.timeController = new TimeController();
        this.hudView = new HUDView(uiState);
        this.minimapView = new MinimapView(mapView.getCamera(), animationEngine);
        this.controlPanes = new ControlPanes(uiState, timeController, onSaveRequested, onLeaveRequested);

        BorderPane topOverlay = new BorderPane();
        topOverlay.setLeft(hudView);
        topOverlay.setRight(controlPanes.getSpeedPane());
        BorderPane.setAlignment(hudView, Pos.TOP_LEFT);
        BorderPane.setAlignment(controlPanes.getSpeedPane(), Pos.TOP_RIGHT);
        BorderPane.setMargin(hudView, new Insets(HUD_TOP_MARGIN, 0, 0, 0));
        BorderPane.setMargin(controlPanes.getSpeedPane(), new Insets(SPEED_PANE_TOP_MARGIN, 16, 0, 0));

        // Layout
        HBox leftPlayArea = new HBox(BUILD_PANE_TO_MAP_GAP, controlPanes.getBuildPane(), mapView);
        leftPlayArea.setAlignment(Pos.TOP_LEFT);
        leftPlayArea.setFillHeight(true);
        leftPlayArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(mapView, Priority.ALWAYS);
        mapView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        controlPanes.getBuildPane().setMaxHeight(Double.MAX_VALUE);

        VBox rightOverlay = new VBox(12, minimapView);
        rightOverlay.setAlignment(Pos.TOP_RIGHT);
        rightOverlay.setFillWidth(false);
        rightOverlay.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane center = new StackPane(leftPlayArea, rightOverlay);
        center.setAlignment(Pos.TOP_LEFT);
        center.setPadding(Insets.EMPTY);
        center.setStyle(CENTER_BACKGROUND_STYLE);
        StackPane.setAlignment(leftPlayArea, Pos.TOP_LEFT);
        StackPane.setAlignment(rightOverlay, Pos.TOP_RIGHT);
        this.gameOverOverlay = new GameOverPane(this.onRestartRequested, this.onLeaveRequested);
        center.getChildren().add(gameOverOverlay);
        StackPane.setAlignment(gameOverOverlay, Pos.CENTER);
        this.setCenter(center);
        this.setTop(topOverlay);

        // -----------------------------
        // Connect Model -> View
        // -----------------------------
        GameMap map = game.getWorld().getMap();
        mapView.setMap(map);
        mapView.setCompany(company);
        mapView.setUIState(uiState);
        minimapView.setMap(map);
        minimapView.setCompany(company);

        // -----------------------------
        // Controllers
        // -----------------------------
        this.inputController = new InputController();
        this.selectionController = new SelectionController();

        BuildController buildController = new BuildController(game.getWorld(), game.getCompany());
        FleetController fleetController = new FleetController(game.getCompany(), game.getWorld());

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
        // Input binding (JavaFX -> Controller)
        // -----------------------------
        setupInput();

        // -----------------------------
        // Start game loop
        // -----------------------------
        gameController.start();
    }

    private void setupInput() {
        mapView.setOnMousePressed(event -> {
            this.requestFocus();
            inputController.onMousePressed(event);
        });
        mapView.setOnMouseReleased(inputController::onMouseReleased);
        mapView.setOnMouseDragged(event -> {
            this.requestFocus();
            inputController.onMouseDragged(event);
        });
        minimapView.setOnMousePressed(event -> {
            this.requestFocus();
            gameController.handleMinimapInput(event.getX(), event.getY());
            event.consume();
        });
        minimapView.setOnMouseDragged(event -> {
            this.requestFocus();
            gameController.handleMinimapInput(event.getX(), event.getY());
            event.consume();
        });

        this.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            try {
                inputController.onKeyPressed(e);
                e.consume();
            } catch (Exception ignored) {
            }
        });

        this.setFocusTraversable(true);
    }

    public void render() {
        mapView.render();
        minimapView.render();
        boolean showedRevenueMessage = false;
        boolean showedCostMessage = false;
        for (String message : world.drainMessages()) {
            if (world.isCostMessage(message)) {
                hudView.showCostMessage(world.stripMessagePrefix(message));
                showedCostMessage = true;
            } else if (world.isRevenueMessage(message)) {
                hudView.showEarnMessage(world.stripMessagePrefix(message));
                showedRevenueMessage = true;
            }
        }
        Money currentCash = company.getEconomy().getCash();
        Money cashDelta = currentCash.subtract(lastRenderedCash);
        if (cashDelta.isPositive() && !showedRevenueMessage) {
            hudView.showEarnMessage(cashDelta);
        } else if (cashDelta.isNegative() && !showedCostMessage) {
            hudView.showCostMessage("spend -" + cashDelta.abs().amount() + " coins");
        }
        lastRenderedCash = currentCash;
        hudView.render(
                currentCash,
                animationEngine.getFormattedTime(),
                timeController.getSpeed()
        );
        controlPanes.render();

        if (game.isGameOver()) {
            showGameOverOverlay();
        }
    }

    public MapView getMapView() {
        return mapView;
    }

    public MinimapView getMinimapView() {
        return minimapView;
    }

    public UIState getUIState() {
        return uiState;
    }

    public AnimationEngine getAnimationEngine() {
        return animationEngine;
    }

    public HUDView getHudView() {
        return hudView;
    }

    public ControlPanes getControlPanes() {
        return controlPanes;
    }

    public void dispose() {
        gameController.stop();
    }

    private void showGameOverOverlay() {
        if (gameOverOverlayShown) {
            return;
        }
        gameOverOverlayShown = true;
        gameOverOverlay.setManaged(true);
        gameOverOverlay.setVisible(true);
        gameOverOverlay.toFront();
    }
}
