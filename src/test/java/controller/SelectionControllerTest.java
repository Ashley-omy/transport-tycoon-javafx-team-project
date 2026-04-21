package controller;

import common.GridPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectionControllerTest {

    @Test
    void selectTileStoresTileAndClearsVehicleSelection() {
        SelectionController controller = new SelectionController();

        controller.selectTile(new GridPos(1, 1));

        assertEquals(new GridPos(1, 1), controller.getSelectedTile());
        assertTrue(controller.getSelectedVehicleId() == null);
    }

    @Test
    void clearRemovesTileAndVehicleSelection() {
        SelectionController controller = new SelectionController();
        controller.selectTile(new GridPos(2, 2));
        controller.selectVehicle("vehicle-1");

        controller.clear();

        assertTrue(controller.getSelectedTile() == null);
        assertTrue(controller.getSelectedVehicleId() == null);
    }
}
