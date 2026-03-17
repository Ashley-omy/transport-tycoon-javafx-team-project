package model;

import common.Id;

public class Bus extends Vehicle {
    public Bus(Id id, int capacityUnits) {
        super(id, capacityUnits);
    }

    @Override
    public boolean acceptsKind(ShipmentKind kind) {
        return kind == ShipmentKind.PASSENGERS;
    }

    @Override
    public boolean acceptsGoodsType(GoodsType goodsType) {
        return false; // buses do not carry goods
    }
}