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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import model.Company;
import model.Game;
import model.GameMap;
import model.World;

public class GameWindow extends BorderPane {
    // --- View ---
    private final MapView mapView;
    private final HUDView hudView;
    private final MinimapView minimapView;
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
        this.timeController = new TimeController();
        this.hudView = new HUDView(uiState, timeController);
        this.minimapView = new MinimapView(mapView.getCamera());

        // Layout
        StackPane center = new StackPane(mapView, minimapView);
        StackPane.setAlignment(minimapView, Pos.TOP_RIGHT);
        StackPane.setMargin(minimapView, new Insets(16));
        this.setCenter(center);
        this.setTop(hudView);

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
        mapView.setOnMousePressed(inputController::onMousePressed);
        mapView.setOnMouseReleased(inputController::onMouseReleased);
        mapView.setOnMouseDragged(inputController::onMouseDragged);
        minimapView.setOnMousePressed(event -> {
            gameController.handleMinimapInput(event.getX(), event.getY());
            event.consume();
        });
        minimapView.setOnMouseDragged(event -> {
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
        hudView.render(
                company.getEconomy().getCash(),
                animationEngine.getFormattedTime(),
                timeController.getSpeed()
        );
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
}
