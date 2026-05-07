/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import common.*;
import java.util.*;

public abstract class MapEntity implements java.io.Serializable {
    // Default lifetime for transient floating messages above an entity.
    private static final double DEFAULT_EVENT_DISPLAY_SECONDS = 2.0;

    protected Id id;
    protected int footprintW;
    protected List<Stop> servedStops = new ArrayList<>();
    protected List<Tile> occupiedTiles = new ArrayList<>();
    private final List<EntityEventDisplay> activeEventDisplays = new ArrayList<>();

    public MapEntity(Id id, int footprintW) {
        this.id = id;
        this.footprintW = footprintW;
        this.servedStops = new ArrayList<>();
        this.occupiedTiles = new ArrayList<>();
    }

    public int getFootprintW() {
        return footprintW;
    }

    public Id getId() {
        return id;
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

    public int getServedStopCount() {
        return servedStops.size();
    }

    // Convenience overload: show a message for the default duration.
    public void pushEventDisplay(String text) {
        pushEventDisplay(text, DEFAULT_EVENT_DISPLAY_SECONDS);
    }

    // Add a short-lived message that can be rendered above this entity.
    public void pushEventDisplay(String text, double durationSeconds) {
        if (text == null || text.isBlank()) {
            return;
        }
        if (Double.isNaN(durationSeconds) || Double.isInfinite(durationSeconds) || durationSeconds <= 0.0) {
            return;
        }
        activeEventDisplays.add(new EntityEventDisplay(text, durationSeconds));
    }

    // Return only the text currently active for rendering.
    public List<String> getActiveEventDisplayTexts() {
        if (activeEventDisplays.isEmpty()) {
            return List.of();
        }
        List<String> texts = new ArrayList<>(activeEventDisplays.size());
        for (EntityEventDisplay display : activeEventDisplays) {
            texts.add(display.text);
        }
        return Collections.unmodifiableList(texts);
    }

    // Decrease remaining lifetime for messages and drop expired entries.
    public void tickEventDisplays(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) {
            return;
        }

        Iterator<EntityEventDisplay> iterator = activeEventDisplays.iterator();
        while (iterator.hasNext()) {
            EntityEventDisplay display = iterator.next();
            display.remainingSeconds -= deltaTime;
            if (display.remainingSeconds <= 0.0) {
                iterator.remove();
            }
        }
    }

    public abstract void tick(double deltaTime);

    public void emitSupplyToStops() { }

    public void acceptDelivery(Shipment s) { }

    // Internal DTO for one floating message with its remaining time budget.
    private static final class EntityEventDisplay implements java.io.Serializable {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final String text;
        private double remainingSeconds;

        private EntityEventDisplay(String text, double remainingSeconds) {
            this.text = text;
            this.remainingSeconds = remainingSeconds;
        }
    }
}
