/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */
import common.Vec2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Tile;

public class EntityRenderer {

    public void draw(GraphicsContext gc, Tile t, Vec2 pos, int size) {

        if (t.getStop() != null) {
            gc.setFill(Color.YELLOW);
            gc.fillOval(pos.x + size * 0.3, pos.y + size * 0.3,
                    size * 0.4, size * 0.4);
        }

        if (t.getGarage() != null) {
            gc.setFill(Color.ORANGE);
            gc.fillRect(pos.x + size * 0.25, pos.y + size * 0.25,
                    size * 0.5, size * 0.5);
        }

        if (t.getEntity() != null) {
            gc.setFill(Color.DARKRED);
            gc.fillRect(pos.x + size * 0.1, pos.y + size * 0.1,
                    size * 0.8, size * 0.8);
        }
    }
}
