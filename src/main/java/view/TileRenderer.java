/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 * This class renders terrain tiles
 */
import common.Vec2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.City;
import model.Factory;
import model.Mine;
import model.Tile;

public class TileRenderer {

    public void drawTile(GraphicsContext gc, Tile t, Vec2 pos, int size) {

        // Simple color by terrain
        if (t.isWater()) {
            gc.setFill(Color.LIGHTBLUE);
        } else if (t.isForest()) {
            gc.setFill(Color.FORESTGREEN);
        } else {
            gc.setFill(Color.GREEN);
        }

        gc.fillRect(pos.x, pos.y, size, size);

        // Road overlay
        if (t.getRoadPiece() != null) {
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(pos.x, pos.y, size, size);
        }
        boolean hideCityFillForInternalRoad = t.getEntity() instanceof City city
                && t.getRoadPiece() != null
                && city.hasInternalRoadAt(t.getPos());

        if (t.getEntity() != null && !hideCityFillForInternalRoad) {
            if (t.getEntity() instanceof Factory) {
                gc.setFill(Color.GRAY);
            } else if (t.getEntity() instanceof Mine) {
                gc.setFill(Color.SADDLEBROWN);
            } else {
                gc.setFill(Color.ORANGE);
            }
            gc.fillRect(pos.x + size * 0.2, pos.y + size * 0.2,
                    size * 0.6, size * 0.6);
        }
        if (t.getStop() != null) {
            gc.setFill(Color.RED);
            gc.fillRect(pos.x + size * 0.2, pos.y + size * 0.2,
                    size * 0.6, size * 0.6);
        }

        // Grid border
        gc.setStroke(Color.DARKGREEN);
        gc.strokeRect(pos.x, pos.y, size, size);
    }
}
