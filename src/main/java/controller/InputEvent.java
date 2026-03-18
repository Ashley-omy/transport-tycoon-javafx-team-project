/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author asuna
 */
import common.GridPos;
import common.Vec2;

public class InputEvent {

    public String type;
    public Vec2 mousePos;
    public GridPos tilePos;
    public String key;
    public int mouseButton;

    public GridPos dragStartTile;
    public GridPos dragEndTile;

    // Constructor for mouse event
    public InputEvent(String type, Vec2 mousePos, int mouseButton) {
        this.type = type;
        this.mousePos = mousePos;
        this.mouseButton = mouseButton;
    }

    // Constructor for keyboard event
    public InputEvent(String type, String key) {
        this.type = type;
        this.key = key;
    }
}