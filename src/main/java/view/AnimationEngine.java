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
import controller.TimeController;
import model.Game;

public class AnimationEngine {
    private double gameTime;

    public void update(double deltaTime, TimeController timeController) {
        // future animation logic
        gameTime += deltaTime * timeController.getSpeedMultiplier();
    }

    public String getFormattedTime() {
        int totalSeconds = (int) gameTime;

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public double getGameTime(){return gameTime;}

//    public Vec2 interpolateVehicle(common.GridPos tilePos, double progress) {
//        return new Vec2(tilePos.x, tilePos.y);
//    }
}