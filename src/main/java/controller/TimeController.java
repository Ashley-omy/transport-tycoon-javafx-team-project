/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

public class TimeController {

    private TimeSpeed speed = TimeSpeed.NORMAL;

    public void togglePause() {
        speed = (speed == TimeSpeed.PAUSE) ? TimeSpeed.NORMAL : TimeSpeed.PAUSE;
    }

    public void setSpeed(TimeSpeed s) {
        if (s != null) {
            speed = s;
        }
    }
    public TimeSpeed getSpeed(){return speed;}

    public double getSpeedMultiplier() {
        return switch (speed) {
            case PAUSE -> 0.0;
            case NORMAL -> 1.0;
            case FAST -> 2.0;
            case VERY_FAST -> 4.0;
        };
    }
}

