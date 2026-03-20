/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */

import common.GridPos;
import controller.SelectionController;

public class UIState {

    private BuildMode buildMode = BuildMode.NONE;
    private GridPos selectedTile;
    private String selectedVehicleId;

    public void setBuildMode(BuildMode m) {
        this.buildMode = m;
    }

    public BuildMode getBuildMode() {
        return buildMode;
    }

    // Sync state from SelectionController
    public void syncFromSelection(SelectionController sel) {
        this.selectedTile = sel.getSelectedTile();
        this.selectedVehicleId = sel.getSelectedVehicleId();
    }

    public GridPos getSelectedTile() {
        return selectedTile;
    }

    public String getSelectedVehicleId() {
        return selectedVehicleId;
    }
}
