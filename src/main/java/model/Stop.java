package model;

import common.Id;
import common.Money;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Stop {
    private final Id id;
    private final Tile occupiedTile;
    private final MapEntity servedPlace;
    private final Queue<Shipment> queue = new LinkedList<>();

    public Stop(Id id, Tile occupiedTile, MapEntity servedPlace) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (occupiedTile == null) throw new IllegalArgumentException("occupiedTile cannot be null");
        if (servedPlace == null) throw new IllegalArgumentException("servedPlace cannot be null");
        this.id = id;
        this.occupiedTile = occupiedTile;
        this.servedPlace = servedPlace;
    }

    public Id getId() { return id; }
    public Tile getOccupiedTile() { return occupiedTile; }
    public MapEntity getServedPlace() { return servedPlace; }

    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;
        // future: queue aging / local generation hooks
    }

    public void enqueue(Shipment s) {
        if (s == null) throw new IllegalArgumentException("shipment cannot be null");
        queue.add(s);
    }

    public List<Shipment> getQueueSnapshot() {
        return List.copyOf(queue);
    }

    public Shipment dequeueFor(Vehicle vehicle) {
        if (vehicle == null || queue.isEmpty()) return null;

        int free = vehicle.getFreeCapacityUnits();
        if (free <= 0) return null;

        Shipment result = null;
        
        for (Shipment s : new ArrayList<>(queue)) {
            if (!vehicle.canLoad(s)) continue;

            if (s.getUnits() <= free) {
                queue.remove(s);
                if (result == null) {
                    result = s;
                } else {
                    int mergedUnits = result.getUnits() + s.getUnits();
                    result = new Shipment(
                        result.getKind(),
                        result.getGoodsType(),
                        mergedUnits,
                        result.getFromStopId(),
                        result.getToStopId(),
                        result.getValuePerTile()
                    );
                }
                free -= s.getUnits();
            } else {
                Shipment part = s.splitOff(free);
                if (result == null) {
                    result = part;
                } else {
                    int mergedUnits = result.getUnits() + part.getUnits();
                    result = new Shipment(
                        result.getKind(),
                        result.getGoodsType(),
                        mergedUnits,
                        result.getFromStopId(),
                        result.getToStopId(),
                        result.getValuePerTile()
                    );
                }
                break;
            }
        }
        return result;
    }

    public Money deliverFrom(Vehicle vehicle) {
        if (vehicle == null) return Money.ZERO;
        Shipment cargo = vehicle.getCargo();
        if (cargo == null) return Money.ZERO;
        if (!id.equals(cargo.getToStopId())) return Money.ZERO;

        Money payout = cargo.getValuePerTile().multiply(cargo.getUnits());
        vehicle.clearCargo();
        return payout;
    }
}