/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author lenovo
 * This class renders terrain tiles
 */
import common.Vec2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.Tile;

public class TileRenderer {

    public void drawTile(GraphicsContext gc, Tile t, Vec2 pos, int size) {

        // Simple color by terrain
        if (t.isWater()) {
            gc.setFill(Color.LIGHTBLUE);
        } else if (t.isForest()) {
            gc.setFill(Color.FORESTGREEN);
        } else {
            gc.setFill(Color.BEIGE);
        }

        gc.fillRect(pos.x, pos.y, size, size);

        // Grid border
        gc.setStroke(Color.GRAY);
        gc.strokeRect(pos.x, pos.y, size, size);

        // Road overlay (simple)
        if (t.hasRoad()) {
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(pos.x + size * 0.2, pos.y + size * 0.2,
                    size * 0.6, size * 0.6);
        }
    }
}