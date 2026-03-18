package model;

import common.GridPos;
import common.Money;

public class World {
    public static final Money ROAD_BUILD_COST = Money.of(100);

    public boolean canBuildAt(GridPos pos) {
        return true;
    }

    public boolean buildRoad(GridPos pos, Company company) {
        if (company == null) throw new IllegalArgumentException("company cannot be null");
        if (!canBuildAt(pos)) return false;

        if (!company.spend(ROAD_BUILD_COST)) return false;

        return true;
    }

    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;
    }
}