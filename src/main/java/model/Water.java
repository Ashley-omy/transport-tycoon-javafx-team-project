/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class Water extends Terrain {
    @Override
    public boolean isPassable() { return false;}

    @Override
    public double buildMultiplier() {return 2.0;}
}
