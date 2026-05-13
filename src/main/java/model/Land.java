package model;

public class Land extends Terrain {
    @java.io.Serial
    private static final long serialVersionUID = 386549435600124750L;

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
