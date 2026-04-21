package model;

import common.Money;

import java.util.Objects;

public final class BridgeSpec {
    private final BridgeType type;
    private final int maxSpanTiles;
    private final Money cost;
    private final double speedLimit;

    public BridgeSpec(BridgeType type, int maxSpanTiles, Money cost, double speedLimit) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (maxSpanTiles <= 0) {
            throw new IllegalArgumentException("maxSpanTiles must be > 0");
        }
        if (cost == null) {
            throw new IllegalArgumentException("cost cannot be null");
        }
        if (cost.amount() < 0) {
            throw new IllegalArgumentException("cost must be >= 0");
        }
        if (Double.isNaN(speedLimit) || Double.isInfinite(speedLimit) || speedLimit <= 0.0) {
            throw new IllegalArgumentException("speedLimit must be > 0");
        }

        this.type = type;
        this.maxSpanTiles = maxSpanTiles;
        this.cost = cost;
        this.speedLimit = speedLimit;
    }

    public BridgeType getType() {
        return type;
    }

    public int getMaxSpanTiles() {
        return maxSpanTiles;
    }

    public Money getCost() {
        return cost;
    }

    public double getSpeedLimit() {
        return speedLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BridgeSpec that = (BridgeSpec) o;
        return maxSpanTiles == that.maxSpanTiles
                && Double.compare(speedLimit, that.speedLimit) == 0
                && type == that.type
                && Objects.equals(cost, that.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, maxSpanTiles, cost, speedLimit);
    }

    @Override
    public String toString() {
        return "BridgeSpec{"
                + "type=" + type
                + ", maxSpanTiles=" + maxSpanTiles
                + ", cost=" + cost
                + ", speedLimit=" + speedLimit
                + '}';
    }
}
