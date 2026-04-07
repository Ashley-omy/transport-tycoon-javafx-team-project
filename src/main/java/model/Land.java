package model;

public class Land extends Terrain {

    @Override
    public boolean isPassable() {
        return true;
    }

    @Override
    public double buildMultiplier() {
        return 1.0;
    }

    @Override
    public boolean isLand() {
        return true;
    }
}