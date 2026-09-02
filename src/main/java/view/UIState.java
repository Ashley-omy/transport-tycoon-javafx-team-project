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

import java.util.ArrayList;
import java.util.List;

public class UIState {

    private BuildMode buildMode = BuildMode.NONE;
    private GridPos selectedTile;
    private String selectedVehicleId;
    private boolean routePlacementRequested;
    private boolean bridgeTypeSelectionRequested;
    private final List<GridPos> pendingBridgeTiles = new ArrayList<>();

    public void setBuildMode(BuildMode m) {
        this.buildMode = m;
        if (m != BuildMode.ROUTE) {
            this.routePlacementRequested = false;
        }
        if (m != BuildMode.BRIDGE) {
            clearPendingBridgeTiles();
            bridgeTypeSelectionRequested = false;
        }
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

    public void requestRoutePlacement() {
        routePlacementRequested = true;
    }

    public boolean consumeRoutePlacementRequest() {
        boolean requested = routePlacementRequested;
        routePlacementRequested = false;
        return requested;
    }

    public void requestBridgeTypeSelection() {
        bridgeTypeSelectionRequested = true;
    }

    public boolean consumeBridgeTypeSelectionRequest() {
        boolean requested = bridgeTypeSelectionRequested;
        bridgeTypeSelectionRequested = false;
        return requested;
    }

    public boolean addPendingBridgeTile(GridPos pos) {
        if (pos == null || pendingBridgeTiles.contains(pos)) {
            return false;
        }
        pendingBridgeTiles.add(pos);
        return true;
    }

    public boolean hasPendingBridgeTile(GridPos pos) {
        return pendingBridgeTiles.contains(pos);
    }

    public GridPos getLastPendingBridgeTile() {
        if (pendingBridgeTiles.isEmpty()) {
            return null;
        }
        return pendingBridgeTiles.get(pendingBridgeTiles.size() - 1);
    }

    public boolean hasPendingBridgeTiles() {
        return !pendingBridgeTiles.isEmpty();
    }

    public List<GridPos> getPendingBridgeTiles() {
        return List.copyOf(pendingBridgeTiles);
    }

    public void clearPendingBridgeTiles() {
        pendingBridgeTiles.clear();
    }
}
