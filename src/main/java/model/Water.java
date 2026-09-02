package model;

import java.util.Objects;

public class Water extends Terrain {
    @java.io.Serial
    private static final long serialVersionUID = -6345753606454440660L;

    private WaterType waterType;

    public Water() {
        this(WaterType.RIVER);
    }

    public Water(WaterType waterType) {
        this.waterType = Objects.requireNonNull(waterType, "waterType");
    }

    public WaterType getWaterType() {
        return waterType;
    }

    public void setWaterType(WaterType waterType) {
        this.waterType = Objects.requireNonNull(waterType, "waterType");
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Override
    public double buildMultiplier() {
        return 2.0;
    }

    @Override
    public boolean isWater() {
        return true;
    }
}
