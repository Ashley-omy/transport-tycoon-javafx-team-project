/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.GameMap;

public class VehicleRenderer {

    public void draw(GraphicsContext gc, GameMap map, Camera camera, AnimationEngine anim) {

        // TODO: integrate with vehicle list from model

        // placeholder
        gc.setFill(Color.RED);
        gc.fillOval(100, 100, 10, 10);
    }
}
