/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */
import common.GridPos;
import common.Vec2;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import model.Company;
import model.GameMap;

public class MapView extends Pane {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private UIState uiState;

    private Camera camera;
    private Renderer renderer;

    private GameMap map; // injected later
    private Company company;

    public MapView(int width, int height, AnimationEngine animationEngine) {
        if (animationEngine == null) {
            throw new IllegalArgumentException("animationEngine cannot be null");
        }
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        this.getChildren().add(canvas);
        // Keep the view at a fixed canvas size so StackPane alignment can move it.
        this.setPrefSize(width, height);
        this.setMinSize(width, height);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        camera = new Camera(new GridPos(0,0),
                (int) canvas.getWidth(),
                (int) canvas.getHeight());
        this.renderer = new Renderer(animationEngine);
    }

    public void setMap(GameMap map) {
        this.map = map;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setUIState(UIState uiState) {
        this.uiState = uiState;
    }

    public GridPos screenToTile(Vec2 screenPos) {
        return camera.screenToTile(screenPos);
    }

    public void render() {
        if (map == null) return;

        // Clear screen
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        renderer.render(gc, map, camera, uiState, company == null ? null : company.getFleet());
    }
}
