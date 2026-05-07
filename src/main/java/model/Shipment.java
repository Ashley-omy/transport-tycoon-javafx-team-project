package model;

import common.Id;
import common.Money;

public class Shipment implements java.io.Serializable {
    private final ShipmentKind kind;
    private final GoodsType goodsType; // null when kind == PASSENGERS
    private int units;
    private final Id fromStopId;
    private final Id toStopId;
    private final Money valuePerTile;

    public Shipment(
            ShipmentKind kind,
            GoodsType goodsType,
            int units,
            Id fromStopId,
            Id toStopId,
            Money valuePerTile
    ) {
        if (kind == null) throw new IllegalArgumentException("kind cannot be null");
        if (units <= 0) throw new IllegalArgumentException("units must be > 0");
        if (fromStopId == null) throw new IllegalArgumentException("fromStopId cannot be null");
        if (toStopId == null) throw new IllegalArgumentException("toStopId cannot be null");
        if (valuePerTile == null) throw new IllegalArgumentException("valuePerTile cannot be null");

        if (kind == ShipmentKind.GOODS && goodsType == null) {
            throw new IllegalArgumentException("goodsType required for GOODS shipment");
        }
        if (kind == ShipmentKind.PASSENGERS && goodsType != null) {
            throw new IllegalArgumentException("goodsType must be null for PASSENGERS shipment");
        }

        this.kind = kind;
        this.goodsType = goodsType;
        this.units = units;
        this.fromStopId = fromStopId;
        this.toStopId = toStopId;
        this.valuePerTile = valuePerTile;
    }

    public ShipmentKind getKind() { return kind; }
    public GoodsType getGoodsType() { return goodsType; }
    public int getUnits() { return units; }
    public Id getFromStopId() { return fromStopId; }
    public Id getToStopId() { return toStopId; }
    public Money getValuePerTile() { return valuePerTile; }

    public boolean isPassengers() { return kind == ShipmentKind.PASSENGERS; }
    public boolean isGoods() { return kind == ShipmentKind.GOODS; }

    public void removeUnits(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        if (amount > units) throw new IllegalArgumentException("cannot remove more than available units");
        units -= amount;
    }

    public Shipment splitOff(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        if (amount > units) throw new IllegalArgumentException("cannot split more than available units");

        this.units -= amount;

        return new Shipment(
                this.kind,
                this.goodsType,
                amount,
                this.fromStopId,
                this.toStopId,
                this.valuePerTile
        );
    }
}