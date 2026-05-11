package model;

import common.Id;
import common.Money;

public class Truck extends Vehicle {
    @java.io.Serial
    private static final long serialVersionUID = 9006244109055175696L;

    public Truck(Id id, int capacityUnits, Money purchaseCost, Money maintenanceCost, double speed) {
        super(id, capacityUnits, purchaseCost, maintenanceCost, speed);
    }

    @Override
    public boolean acceptsKind(ShipmentKind kind) {
        return kind == ShipmentKind.GOODS;
    }

    @Override
    public boolean acceptsGoodsType(GoodsType goodsType) {
        return goodsType != null; // accepts any goods type
    }
}
