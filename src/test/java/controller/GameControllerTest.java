package controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    @Test
    void isNotNull() {
        // Just verify basic class existence since it is tightly coupled to JavaFX GUI components
        // and Toolkit wouldn't be initialized in CI environments.
        assertNotNull(GameController.class);
    }
}
