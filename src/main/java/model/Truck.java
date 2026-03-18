package model;

import common.Id;
import common.Money;

public class Truck extends Vehicle {

    // null = accepts all goods
    private final GoodsType specialization;

    public Truck(Id id, int capacityUnits, Money purchaseCost, Money maintenanceCost, double speed, GoodsType specialization) {
        super(id, capacityUnits, purchaseCost, maintenanceCost, speed);
        this.specialization = specialization;
    }

    @Override
    public boolean acceptsKind(ShipmentKind kind) {
        return kind == ShipmentKind.GOODS;
    }

    @Override
    public boolean acceptsGoodsType(GoodsType goodsType) {
        if (goodsType == null) return false;
        return specialization == null || specialization == goodsType;
    }
}