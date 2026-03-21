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
import model.Facility;
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

        //Temp color setting
        gc.setFill(Color.GREEN);

        gc.fillRect(pos.x, pos.y, size, size);

        // Grid border
        gc.setStroke(Color.DARKGREEN);
        gc.strokeRect(pos.x, pos.y, size, size);

        // Road overlay (simple)
        if (t.getRoadPiece() != null) {
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(pos.x + size * 0.2, pos.y + size * 0.2,
                    size * 0.6, size * 0.6);
        }
        if (t.getEntity() != null) {
            if(t.getEntity() instanceof Facility){
                gc.setFill(Color.PURPLE);
                gc.fillRect(pos.x + size * 0.2, pos.y + size * 0.2,
                        size * 0.6, size * 0.6);
            }else {
                gc.setFill(Color.ORANGE);
                gc.fillRect(pos.x + size * 0.2, pos.y + size * 0.2,
                        size * 0.6, size * 0.6);
            }
        }
        if (t.getStop() != null) {
            gc.setFill(Color.RED);
            gc.fillRect(pos.x + size * 0.2, pos.y + size * 0.2,
                    size * 0.6, size * 0.6);
        }
    }
}