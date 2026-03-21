package model;

import common.GridPos;
import common.Id;
import common.Money;
import common.Vec2;

public abstract class Vehicle {
    protected final Id id;
    protected final int capacityUnits;
    protected final Money purchaseCost;
    protected final Money maintenanceCost;
    protected final double speed;
    protected Shipment cargo;
    protected Company owner;
    protected VehicleState state;
    protected Route assignedRoute;
    private GridPos tilePost;
    private Vec2 worldPos;
    private Route route;

    protected Vehicle(Id id, int capacityUnits, Money purchaseCost, Money maintenanceCost, double speed) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (capacityUnits <= 0) throw new IllegalArgumentException("capacityUnits must be > 0");
        if (purchaseCost == null) throw new IllegalArgumentException("purchaseCost cannot be null");
        if (maintenanceCost == null) throw new IllegalArgumentException("maintenanceCost cannot be null");
        if (speed <= 0) throw new IllegalArgumentException("speed must be > 0");
        this.id = id;
        this.capacityUnits = capacityUnits;
        this.purchaseCost = purchaseCost;
        this.maintenanceCost = maintenanceCost;
        this.speed = speed;
        this.state = VehicleState.IDLE;
    }

    public Id getId() { return id; }
    public Money getPurchaseCost() { return purchaseCost; }
    public Money getMaintenanceCost() { return maintenanceCost; }
    public double getSpeed() { return speed; }
    
    public void setOwner(Company owner) { this.owner = owner; }

    // Route assignment methods
    public void assignRoute(Route route) {
        if (route == null) throw new IllegalArgumentException("route cannot be null");
        this.assignedRoute = route;
    }
    
    public Route getAssignedRoute() {
        return assignedRoute;
    }
    
    public boolean hasRoute() {
        return assignedRoute != null;
    }
    
    public void clearRoute() {
        this.assignedRoute = null;
        this.state = VehicleState.IDLE;
    }

    // Cargo storage methods
    public Shipment getCargo() { return cargo; }

    public void clearCargo() { this.cargo = null; }

    public int getFreeCapacityUnits() {
        return cargo == null ? capacityUnits : (capacityUnits - cargo.getUnits());
    }

    public boolean hasCargo() {
        return cargo != null && cargo.getUnits() > 0;
    }

    public VehicleState getState() {
        return state;
    }
    
    public void setState(VehicleState state) {
        if (state == null) throw new IllegalArgumentException("state cannot be null");
        this.state = state;
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

        Shipment loaded = stop.dequeueFor(this);
        if (loaded == null) return false;

        if (cargo == null) {
            cargo = loaded;
        } else {
            // merge units into existing cargo
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
        Money payout = stop.deliverFrom(this);
        
        // Pay the company for successful delivery
        if (owner != null && payout.isPositive()) {
            owner.completeShipmentWithPayout(payout);
        }
        
        return payout;
    }

    public abstract boolean acceptsKind(ShipmentKind kind);

    public abstract boolean acceptsGoodsType(GoodsType goodsType);
}