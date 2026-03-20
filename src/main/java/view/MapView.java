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
import model.GameMap;
import model.Tile;

public class MapView extends Pane {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private UIState uiState;

    private Camera camera;
    private Renderer renderer;

    private GameMap map; // injected later

    public MapView(int width, int height) {
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        this.getChildren().add(canvas);

        camera = new Camera(new GridPos(0,0),
                (int) canvas.getWidth(),
                (int) canvas.getHeight());
        this.renderer = new Renderer();
    }

    public void setMap(GameMap map) {
        this.map = map;
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

        renderer.render(gc, map, camera, uiState);
    }
}