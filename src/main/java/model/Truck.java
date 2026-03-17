package model;

import common.Id;

public class Truck extends Vehicle {

    // null = accepts all goods
    private final GoodsType specialization;

    public Truck(Id id, int capacityUnits, GoodsType specialization) {
        super(id, capacityUnits);
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