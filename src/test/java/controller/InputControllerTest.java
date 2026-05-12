package controller;

import common.Vec2;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InputControllerTest {

    @Test
    void canAddAndPollEvents() throws Exception {
        InputController controller = new InputController();
        assertTrue(controller.poll().isEmpty());
        
        // Inject a dummy event using reflection to test the polling logic
        // without depending on JavaFX Toolkit initialization and complex constructors.
        java.lang.reflect.Field field = InputController.class.getDeclaredField("eventQueue");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<InputEvent> queue = (List<InputEvent>) field.get(controller);
        
        queue.add(new InputEvent("MOUSE_DOWN", new Vec2(10.0, 10.0), 1));
        queue.add(new InputEvent("KEY_DOWN", "A"));
        
        List<InputEvent> events = controller.poll();
        assertEquals(2, events.size());
        
        assertEquals("MOUSE_DOWN", events.get(0).type);
        assertEquals(1, events.get(0).mouseButton);
        
        assertEquals("KEY_DOWN", events.get(1).type);
        assertEquals("A", events.get(1).key);
        
        assertTrue(controller.poll().isEmpty(), "Polled events should be cleared");
    }
}
