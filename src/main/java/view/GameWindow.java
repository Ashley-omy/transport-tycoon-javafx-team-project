/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import controller.BuildController;
import controller.ActionResult;
import controller.FleetController;
import controller.GameController;
import controller.InputController;
import controller.SelectionController;
import controller.TimeController;
import common.Money;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Company;
import model.Game;
import model.GameMap;
import model.World;

import java.util.function.BooleanSupplier;

public class GameWindow extends BorderPane {
    private static final String LOGO_PATH = "/assets/logo.png";
    private static final int INITIAL_MAP_VIEW_WIDTH = 1000;
    private static final int INITIAL_MAP_VIEW_HEIGHT = 700;
    private static final int BUILD_PANE_WIDTH = 150;
    private static final int BUILD_PANE_HORIZONTAL_PADDING = 20;
    private static final int BUILD_PANE_TO_MAP_GAP = 16;
    private static final int HUD_LEFT_MARGIN = BUILD_PANE_WIDTH + BUILD_PANE_HORIZONTAL_PADDING + BUILD_PANE_TO_MAP_GAP;
    private static final double HUD_LOGO_WIDTH = 160.0;
    private static final int HUD_LOGO_GAP = 8;
    private static final int HUD_LOGO_LEFT_MARGIN = HUD_LEFT_MARGIN - (int) HUD_LOGO_WIDTH - HUD_LOGO_GAP;
    private static final int HUD_TOP_MARGIN = 10;
    private static final int SPEED_PANE_TOP_MARGIN = 18;
    private static final int MESSAGE_PANE_HEIGHT = 80;
    private static final String PANEL_BACKGROUND_STYLE =
            "-fx-background-color: #ffd669;";
    private static final String MESSAGE_PANE_STYLE =
            "-fx-background-color: rgba(0, 0, 0, 0.5); " +
            "-fx-border-color: white; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 6 10 6 10;";
    private static final String MESSAGE_TEXT_STYLE =
            "-fx-font-size: 20px; -fx-font-weight: bold;";
    private static final Color MESSAGE_SUCCESS_COLOR = Color.rgb(120, 255, 120);
    private static final Color MESSAGE_ERROR_COLOR = Color.rgb(255, 120, 120);

    // --- View ---
    private final MapView mapView;
    private final HUDView hudView;
    private final MinimapView minimapView;
    private final ControlPanes controlPanes;
    private final Label messageLabel;
    private final UIState uiState;
    private final Game game;
    private final GameOverPane gameOverOverlay;
    private boolean gameOverOverlayShown;

    // --- Controllers ---
    private final InputController inputController;
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
        Runnable restartAction = onRestartRequested == null ? () -> { } : onRestartRequested;
        Runnable leaveAction = onLeaveRequested == null ? () -> { } : onLeaveRequested;
        BooleanSupplier saveAction = onSaveRequested == null ? () -> false : onSaveRequested;
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
        this.controlPanes = new ControlPanes(uiState, timeController, saveAction, leaveAction);
        this.messageLabel = new Label("");
        StackPane messagePane = new StackPane(messageLabel);
        this.controlPanes.setBuildResultConsumer(this::displayActionMessage);

        ImageView hudLogoView = createHudLogoView();
        HBox logoAndHud = new HBox(HUD_LOGO_GAP, hudLogoView, hudView);
        logoAndHud.setAlignment(Pos.TOP_LEFT);

        BorderPane topOverlay = new BorderPane();
        topOverlay.setStyle(PANEL_BACKGROUND_STYLE);
        topOverlay.setLeft(logoAndHud);
        topOverlay.setRight(controlPanes.getSpeedPane());
        BorderPane.setAlignment(logoAndHud, Pos.TOP_LEFT);
        BorderPane.setAlignment(controlPanes.getSpeedPane(), Pos.TOP_RIGHT);
        BorderPane.setMargin(logoAndHud, new Insets(HUD_TOP_MARGIN, 0, 0, Math.max(0, HUD_LOGO_LEFT_MARGIN)));
        BorderPane.setMargin(controlPanes.getSpeedPane(), new Insets(SPEED_PANE_TOP_MARGIN, 16, 0, 0));

        messagePane.setAlignment(Pos.CENTER);
        messagePane.setMinHeight(MESSAGE_PANE_HEIGHT);
        messagePane.setPrefHeight(MESSAGE_PANE_HEIGHT);
        messagePane.setMaxHeight(MESSAGE_PANE_HEIGHT);
        messagePane.setStyle(MESSAGE_PANE_STYLE);
        messagePane.setMouseTransparent(true);
        messageLabel.setStyle(MESSAGE_TEXT_STYLE);
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        // Layout
        StackPane mapStack = new StackPane(mapView, messagePane);
        mapStack.setAlignment(Pos.TOP_LEFT);
        mapStack.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(mapView, Pos.TOP_LEFT);
        StackPane.setAlignment(messagePane, Pos.TOP_CENTER);
        StackPane.setMargin(messagePane, new Insets(15, 0, 0, -100));
        HBox.setHgrow(mapStack, Priority.ALWAYS);
        messagePane.prefWidthProperty().bind(mapView.widthProperty().multiply(0.5));
        messagePane.maxWidthProperty().bind(mapView.widthProperty().multiply(0.5));

        HBox leftPlayArea = new HBox(BUILD_PANE_TO_MAP_GAP, controlPanes.getBuildPane(), mapStack);
        leftPlayArea.setAlignment(Pos.TOP_LEFT);
        leftPlayArea.setFillHeight(true);
        leftPlayArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(mapStack, Priority.ALWAYS);
        mapView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        controlPanes.getBuildPane().setMaxHeight(Double.MAX_VALUE);

        VBox rightOverlay = new VBox(12, minimapView);
        rightOverlay.setAlignment(Pos.TOP_RIGHT);
        rightOverlay.setFillWidth(false);
        rightOverlay.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane center = new StackPane(leftPlayArea, rightOverlay);
        center.setAlignment(Pos.TOP_LEFT);
        center.setPadding(Insets.EMPTY);
        center.setStyle(PANEL_BACKGROUND_STYLE);
        StackPane.setAlignment(leftPlayArea, Pos.TOP_LEFT);
        StackPane.setAlignment(rightOverlay, Pos.TOP_RIGHT);
        this.gameOverOverlay = new GameOverPane(restartAction, leaveAction);
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
        SelectionController selectionController = new SelectionController();

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
                displayMessage(world.stripMessagePrefix(message), MESSAGE_ERROR_COLOR);
                showedCostMessage = true;
            } else if (world.isRevenueMessage(message)) {
                displayMessage(world.stripMessagePrefix(message), MESSAGE_SUCCESS_COLOR);
                showedRevenueMessage = true;
            }
        }
        Money currentCash = company.getEconomy().getCash();
        Money cashDelta = currentCash.subtract(lastRenderedCash);
        if (cashDelta.isPositive() && !showedRevenueMessage) {
            displayMessage("earn +" + cashDelta.amount() + " coins", MESSAGE_SUCCESS_COLOR);
        } else if (cashDelta.isNegative() && !showedCostMessage) {
            displayMessage("spend -" + cashDelta.abs().amount() + " coins", MESSAGE_ERROR_COLOR);
        }
        lastRenderedCash = currentCash;
        hudView.render(
                currentCash,
                game.getFormattedTime(),
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

    private ImageView createHudLogoView() {
        var resource = GameWindow.class.getResource(LOGO_PATH);
        ImageView logoView = new ImageView();
        if (resource == null) {
            return logoView;
        }
        Image image = new Image(resource.toExternalForm());
        logoView.setImage(image);
        logoView.setFitWidth(HUD_LOGO_WIDTH);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);
        return logoView;
    }

    private void displayActionMessage(ActionResult result) {
        if (result == null || result.getMessage() == null || result.getMessage().isBlank()) {
            return;
        }
        Color textColor = result.isSuccess() ? MESSAGE_SUCCESS_COLOR : MESSAGE_ERROR_COLOR;
        displayMessage(result.getMessage(), textColor);
    }

    private void displayMessage(String text, Color color) {
        if (text == null || text.isBlank()) {
            return;
        }
        messageLabel.setTextFill(color == null ? Color.WHITE : color);
        messageLabel.setText(text);
    }
}
