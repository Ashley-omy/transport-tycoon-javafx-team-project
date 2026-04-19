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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Company;
import model.Game;
import model.GameMap;
import model.World;

public class GameWindow extends BorderPane {
    private static final int MAP_VIEW_WIDTH = 1000;
    private static final int MAP_VIEW_HEIGHT = 700;
    private static final int BUILD_PANE_TO_MAP_GAP = 16;

    // --- View ---
    private final MapView mapView;
    private final HUDView hudView;
    private final MinimapView minimapView;
    private final ControlPanes controlPanes;
    private final UIState uiState;

    // --- Controllers ---
    private final InputController inputController;
    private final SelectionController selectionController;
    private final GameController gameController;
    private final World world;
    private final Company company;
    private final TimeController timeController;
    private final AnimationEngine animationEngine;
    private Money lastRenderedCash;

    public GameWindow(Game game, World world, Company company) {
        this.world = world;
        this.company = company;
        this.animationEngine = new AnimationEngine();
        this.lastRenderedCash = company.getEconomy().getCash();

        // -----------------------------
        // UI State
        // -----------------------------
        this.uiState = new UIState();

        // -----------------------------
        // Views
        // -----------------------------
        this.mapView = new MapView(MAP_VIEW_WIDTH, MAP_VIEW_HEIGHT);
        this.timeController = new TimeController();
        this.hudView = new HUDView(uiState);
        this.minimapView = new MinimapView(mapView.getCamera());
        this.controlPanes = new ControlPanes(uiState, timeController);

        StackPane topOverlay = new StackPane(hudView, controlPanes.getSpeedPane());
        StackPane.setAlignment(hudView, Pos.TOP_LEFT);
        StackPane.setAlignment(controlPanes.getSpeedPane(), Pos.TOP_RIGHT);
        StackPane.setMargin(controlPanes.getSpeedPane(), new Insets(0, 16, 0, 0));

        // Layout
        VBox rightOverlay = new VBox(12, minimapView);
        rightOverlay.setAlignment(Pos.TOP_RIGHT);
        rightOverlay.setFillWidth(false);
        rightOverlay.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane center = new StackPane(mapView, rightOverlay, controlPanes.getBuildPane());
        StackPane.setAlignment(mapView, Pos.CENTER_RIGHT);
        StackPane.setAlignment(rightOverlay, Pos.TOP_RIGHT);
        StackPane.setMargin(rightOverlay, new Insets(16));
        StackPane.setAlignment(controlPanes.getBuildPane(), Pos.TOP_RIGHT);
        StackPane.setMargin(
                controlPanes.getBuildPane(),
                new Insets(16, MAP_VIEW_WIDTH + BUILD_PANE_TO_MAP_GAP, 0, 0)
        );
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
        world.drainDebugMessages();
        Money currentCash = company.getEconomy().getCash();
        if (currentCash.greaterThan(lastRenderedCash)) {
            hudView.showEarnMessage(currentCash.subtract(lastRenderedCash));
        }
        lastRenderedCash = currentCash;
        hudView.render(
                currentCash,
                animationEngine.getFormattedTime(),
                timeController.getSpeed()
        );
        controlPanes.render();
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
}
