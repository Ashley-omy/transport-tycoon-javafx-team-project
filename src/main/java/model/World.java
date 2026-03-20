package model;

import common.GridPos;
import common.Money;

public class World {

    private GameMap map;

    public World(int width, int height){
        this.map = new GameMap(width, height);
    }

    public static final Money ROAD_BUILD_COST = Money.of(150);

    public boolean canBuildAt(GridPos pos) {
        return true;
    }
    public GameMap getMap(){ return map;}

    public boolean buildRoad(GridPos pos, Company company) {
        if (company == null) throw new IllegalArgumentException("company cannot be null");
        if (!canBuildAt(pos)) return false;

        if (!company.getEconomy().spend(ROAD_BUILD_COST, TransactionType.ROAD_CONSTRUCTION, 
                                        "Built road at " + pos)) {
            return false;
        }

        return true;
    }

    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;
    }
}