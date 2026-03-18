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
import model.Tile;

public class TileRenderer {

    private static final int SIZE = 32;

    public void drawTile(GraphicsContext gc, Tile t, int x, int y) {

        gc.setStroke(Color.LIGHTGRAY);
        gc.strokeRect(x * SIZE, y * SIZE, SIZE, SIZE);

        gc.setFill(Color.BEIGE);
        gc.fillRect(x * SIZE, y * SIZE, SIZE, SIZE);
    }
}