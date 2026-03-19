/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author lenovo
 */
import common.GridPos;

public class SelectionController {

    private GridPos selectedTile;
    private String selectedVehicleId;

    public void selectTile(GridPos pos) {
        this.selectedTile = pos;
        this.selectedVehicleId = null;
    }

    public void selectVehicle(String id) {
        this.selectedVehicleId = id;
        this.selectedTile = null;
    }

    public void clear() {
        selectedTile = null;
        selectedVehicleId = null;
    }

    public GridPos getSelectedTile() {
        return selectedTile;
    }

    public String getSelectedVehicleId() {
        return selectedVehicleId;
    }
}
