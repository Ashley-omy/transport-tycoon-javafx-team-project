package controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeControllerTest {

    @Test
    void newControllerStartsAtNormalSpeed() {
        TimeController controller = new TimeController();

        assertEquals(TimeSpeed.NORMAL, controller.getSpeed());
        assertEquals(1.0, controller.getSpeedMultiplier(), 0.0);
    }

    @Test
    void setSpeedFastUpdatesMultiplierToTwo() {
        TimeController controller = new TimeController();

        controller.setSpeed(TimeSpeed.FAST);

        assertEquals(2.0, controller.getSpeedMultiplier(), 0.0);
    }

    @Test
    void togglePauseSwitchesBetweenPauseAndNormal() {
        TimeController controller = new TimeController();
        controller.setSpeed(TimeSpeed.NORMAL);

        controller.togglePause();
        assertEquals(TimeSpeed.PAUSE, controller.getSpeed());
        assertEquals(0.0, controller.getSpeedMultiplier(), 0.0);

        controller.togglePause();
        assertEquals(TimeSpeed.NORMAL, controller.getSpeed());
        assertEquals(1.0, controller.getSpeedMultiplier(), 0.0);
    }
}
