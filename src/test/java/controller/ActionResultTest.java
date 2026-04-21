package controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionResultTest {

    @Test
    void successCreatesSuccessfulResultWithMessage() {
        ActionResult result = ActionResult.success("Yay");

        assertTrue(result.isSuccess());
        assertEquals("Yay", result.getMessage());
    }

    @Test
    void failCreatesFailedResultWithMessage() {
        ActionResult result = ActionResult.fail("Oops");

        assertFalse(result.isSuccess());
        assertEquals("Oops", result.getMessage());
    }
}
