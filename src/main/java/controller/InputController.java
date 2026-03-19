/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author lenovo
 */

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class InputController {

    private final List<InputEvent> eventQueue = new ArrayList<>();

    // Called from JavaFX mouse event
    public void onMousePressed(MouseEvent e) {
        eventQueue.add(new InputEvent(
                "MOUSE_DOWN",
                new common.Vec2(e.getX(), e.getY()),
                e.getButton().ordinal()
        ));
    }

    public void onMouseReleased(MouseEvent e) {
        eventQueue.add(new InputEvent(
                "MOUSE_UP",
                new common.Vec2(e.getX(), e.getY()),
                e.getButton().ordinal()
        ));
    }

    public void onMouseDragged(MouseEvent e) {
        eventQueue.add(new InputEvent(
                "MOUSE_DRAG",
                new common.Vec2(e.getX(), e.getY()),
                e.getButton().ordinal()
        ));
    }

    public void onKeyPressed(KeyEvent e) {
        eventQueue.add(new InputEvent(
                "KEY_DOWN",
                e.getCode().toString() //get key code in String e.g. "W", "SPACE"
        ));
    }

    // Return and clear all events
    public List<InputEvent> poll() {
        List<InputEvent> copy = new ArrayList<>(eventQueue);
        eventQueue.clear();
        return copy;
    }
}