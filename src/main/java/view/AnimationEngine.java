/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author lenovo
 */
import common.Vec2;

public class AnimationEngine {

    public void update(double deltaTime) {
        // future animation logic
    }

    public Vec2 interpolateVehicle(common.GridPos tilePos, double progress) {
        return new Vec2(tilePos.x, tilePos.y);
    }
}