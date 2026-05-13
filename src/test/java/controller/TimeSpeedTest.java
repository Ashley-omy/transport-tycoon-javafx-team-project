package controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TimeSpeedTest {

    @Test
    void enumValuesExist() {
        assertNotNull(TimeSpeed.valueOf("PAUSE"));
        assertNotNull(TimeSpeed.valueOf("NORMAL"));
        assertNotNull(TimeSpeed.valueOf("FAST"));
        assertNotNull(TimeSpeed.valueOf("VERY_FAST"));
    }
}
