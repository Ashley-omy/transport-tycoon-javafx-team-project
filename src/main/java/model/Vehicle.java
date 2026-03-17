package model;

import common.Id;
import common.Money;

public abstract class Vehicle {
    protected final Id id;
    protected final int capacityUnits;
    protected Shipment cargo;

    protected Vehicle(Id id, int capacityUnits) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (capacityUnits <= 0) throw new IllegalArgumentException("capacityUnits must be > 0");
        this.id = id;
        this.capacityUnits = capacityUnits;
    }

    public Id getId() { return id; }

    public Shipment getCargo() { return cargo; }

    public void clearCargo() { this.cargo = null; }

    public int getFreeCapacityUnits() {
        return cargo == null ? capacityUnits : (capacityUnits - cargo.getUnits());
    }

    public boolean hasCargo() {
        return cargo != null && cargo.getUnits() > 0;
    }

    public boolean canLoad(Shipment s) {
        if (s == null) return false;
        if (getFreeCapacityUnits() <= 0) return false;

        if (!acceptsKind(s.getKind())) return false;
        if (s.isGoods() && !acceptsGoodsType(s.getGoodsType())) return false;

        if (cargo == null) return true;

        // if already carrying cargo, must be same kind
        if (cargo.getKind() != s.getKind()) return false;
        if (cargo.isGoods() && cargo.getGoodsType() != s.getGoodsType()) return false;

        return true;
    }

    public boolean loadFrom(Stop stop) {
        if (stop == null) return false;

        Shipment loaded = (Shipment) stop.dequeueFor(this);
        if (loaded == null) return false;

        if (cargo == null) {
            cargo = loaded;
        } else {
            // merge units into existing cargo (same type guaranteed by canLoad)
            int mergedUnits = cargo.getUnits() + loaded.getUnits();
            cargo = new Shipment(
                    cargo.getKind(),
                    cargo.getGoodsType(),
                    mergedUnits,
                    cargo.getFromStopId(),
                    cargo.getToStopId(),
                    cargo.getValuePerTile()
            );
        }
        return true;
    }

    public Money unloadTo(Stop stop) {
        if (stop == null) return Money.ZERO;
        return stop.deliverFrom(this);
    }

    public abstract boolean acceptsKind(ShipmentKind kind);

    public abstract boolean acceptsGoodsType(GoodsType goodsType);
}