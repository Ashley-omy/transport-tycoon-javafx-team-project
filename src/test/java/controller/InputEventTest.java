package controller;

import common.Vec2;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InputEventTest {

    @Test
    void mouseConstructorStetsFields() {
        Vec2 pos = new Vec2(10.0, 20.0);
        InputEvent event = new InputEvent("MOUSE_DOWN", pos, 1);
        assertEquals("MOUSE_DOWN", event.type);
        assertEquals(pos, event.mousePos);
        assertEquals(1, event.mouseButton);
        assertNull(event.key);
    }

    @Test
    void keyConstructorSetsFields() {
        InputEvent event = new InputEvent("KEY_DOWN", "W");
        assertEquals("KEY_DOWN", event.type);
        assertEquals("W", event.key);
        assertNull(event.mousePos);
    }
}
