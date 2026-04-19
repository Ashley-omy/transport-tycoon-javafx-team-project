package model;

/*
  1. Trees can appear on empty tiles.    World.java
  2. There can be 1-4 trees on a tile.   Forest.java
  3. Over time, the number of trees on a tile can increase (e.g., 1 → 4),  Forest.java
  4. and new trees can appear on adjacent empty tiles.   World.java
  5. Roads can also be built on forested tiles, but at a higher cost (clearing).  BuildController.java
 */

public class Forest extends Terrain {
    static final double GROWTH_INTERVAL_SECONDS = 12.0;
    static final double SPREAD_INTERVAL_SECONDS = 18.0;

    private int trees;
    private double growthTimer;
    private double spreadTimer;

    public Forest() {
        this.trees = 1;
        this.growthTimer = 0.0;
        this.spreadTimer = 0.0;
    }

    public int getTrees() {
        return trees;
    }

    /**
     * Forest growth logic driven by accumulated simulation time.
     */
    public void grow(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) {
            return;
        }

        if (trees >= 4) {
            growthTimer = 0.0;
            return;
        }

        growthTimer += deltaTime;
        while (growthTimer >= GROWTH_INTERVAL_SECONDS && trees < 4) {
            growthTimer -= GROWTH_INTERVAL_SECONDS;
            trees++;
        }
    }

    public int consumeSpreadAttempts(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) {
            return 0;
        }

        if (trees < 4) {
            spreadTimer = 0.0;
            return 0;
        }

        spreadTimer += deltaTime;
        int attempts = (int) (spreadTimer / SPREAD_INTERVAL_SECONDS);
        if (attempts > 0) {
            spreadTimer -= attempts * SPREAD_INTERVAL_SECONDS;
        }
        return attempts;
    }

    @Override
    public boolean isPassable() {
        return true;
    }

    @Override
    public double buildMultiplier() {
        return 1.5;
    }

    @Override
    public boolean isForest() {
        return true;
    }
}
