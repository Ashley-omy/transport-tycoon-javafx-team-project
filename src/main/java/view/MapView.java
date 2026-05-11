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
import model.Company;
import model.GameMap;

public class MapView extends Pane {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final double initialWidth;
    private final double initialHeight;
    private UIState uiState;

    private final Camera camera;
    private final Renderer renderer;

    private GameMap map; // injected later
    private Company company;

    public MapView(int width, int height, AnimationEngine animationEngine) {
        if (animationEngine == null) {
            throw new IllegalArgumentException("animationEngine cannot be null");
        }
        this.initialWidth = width;
        this.initialHeight = height;
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        this.getChildren().add(canvas);
        this.setPrefSize(width, height);
        this.setMinSize(0, 0);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        camera = new Camera(new GridPos(0,0), width, height);
        this.renderer = new Renderer(animationEngine);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    protected double computePrefWidth(double height) {
        return initialWidth;
    }

    @Override
    protected double computePrefHeight(double width) {
        return initialHeight;
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        canvas.setWidth(width);
        canvas.setHeight(height);
        camera.setViewportSize((int) Math.round(width), (int) Math.round(height));
        if (map != null) {
            camera.setTopLeftClamped(map, camera.getTopLeftTile());
        }
    }

    public void setMap(GameMap map) {
        this.map = map;
        if (map != null) {
            camera.setTopLeftClamped(map, camera.getTopLeftTile());
        }
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

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        renderer.render(gc, map, camera, uiState, company == null ? null : company.getFleet());
    }
}
