/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import common.*;
import java.util.*;

public abstract class MapEntity {
    protected final Id id;
    protected final List<Stop> servedStops = new ArrayList<>();
    protected final List<Tile> occupiedTiles = new ArrayList<>();

    public MapEntity(Id id) {
        this.id = id;
    }

    public void attachStop(Stop s) {
        servedStops.add(s);
    }

    public void detachStop(Stop s) {
        servedStops.remove(s);
    }

    public abstract void tick(double deltaTime);

    public void emitSupplyToStops() {
        // do later
    }

    public void acceptDelivery(Shipment s) {
        // do later
    }
}