/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.List;
import model.Tile;

public abstract class Terrain {
    private List<Tile> occupiedTiles;

    public abstract boolean isPassable();
    public abstract double buildMultiplier();

    public boolean isWater() {return false;}
    public boolean isForest() {return false;}
}
