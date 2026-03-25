/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class Forest extends Terrain {
    @Override
    public boolean isPassable() { return true; }

    @Override
    public double buildMultiplier() { return 1.5; }

    @Override
    public boolean isForest() {return true;}
}
