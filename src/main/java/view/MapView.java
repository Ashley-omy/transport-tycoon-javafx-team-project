/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author lenovo
 */
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import model.GameMap;
import common.GridPos;
import common.Vec2;

public class MapView extends Canvas {

    private Camera camera;
    private Renderer renderer;

    // For drag movement
    private double lastMouseX;
    private double lastMouseY;

    public MapView() {
        this.camera = new Camera();
        this.renderer = new Renderer();

        setWidth(800);
        setHeight(600);

        // Enable keyboard focus
        setFocusTraversable(true);

        // Mouse drag → camera pan
        setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        //setOnMouseDragged(this::handleDrag);

//        // Optional: keyboard pan (WASD or arrows)
//        setOnKeyPressed(this::handleKey);
    }
/*
//implement later
    // Dragging moves the camera
    private void handleDrag(MouseEvent e) {

        double dx = e.getX() - lastMouseX;
        double dy = e.getY() - lastMouseY;

        // Convert pixel movement → tile movement
        int tileDX = (int)(-dx / 32);
        int tileDY = (int)(-dy / 32);

        if (tileDX != 0 || tileDY != 0) {
            camera.pan(tileDX, tileDY);

            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

     //Keyboard movement
    private void handleKey(KeyEvent e) {
        switch (e.getCode()) {
            case W, UP -> camera.pan(0, -1);
            case S, DOWN -> camera.pan(0, 1);
            case A, LEFT -> camera.pan(-1, 0);
            case D, RIGHT -> camera.pan(1, 0);
        }
    }
*/
    public void render(GameMap map) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        if (map != null) {
            renderer.render(gc, map, camera);
        }
    }

    public GridPos screenToTile(Vec2 p) {
        int x = (int)(p.x / 32) + camera.getTopLeftTile().x;
        int y = (int)(p.y / 32) + camera.getTopLeftTile().y;
        return new GridPos(x, y);
    }

    public Camera getCamera() {
        return camera;
    }
}