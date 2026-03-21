/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import common.*;
import java.util.*;

public abstract class MapEntity {
    protected Id id;
    protected int footprintW;
    protected List<Stop> servedStops = new ArrayList<>();
    protected List<Tile> occupiedTiles = new ArrayList<>();

    public MapEntity(Id id, int footprintW) {
        this.id = id;
        this.footprintW = footprintW;
        this.servedStops = new ArrayList<>();
        this.occupiedTiles = new ArrayList<>();
    }

    public int getFootprintW() {
        return footprintW;
    }

    public List<Tile> getOccupiedTiles() {
        return occupiedTiles;
    }

    public boolean occupies(GridPos pos) {
        for (Tile t : occupiedTiles) {
            if (t.getPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    public void attachStop(Stop s) {
        if (!servedStops.contains(s)) {
            servedStops.add(s);
        }
    }

    public void detachStop(Stop s) {
        servedStops.remove(s);
    }

    public abstract void tick(double deltaTime);

    public void emitSupplyToStops() { }

    public void acceptDelivery(Shipment s) { }
}