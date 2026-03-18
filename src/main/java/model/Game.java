package model;

public class Game {
    private final World world;
    private final Company company;

    private long tick = 0L;
    private boolean paused = false;
    private double speedMultiplier = 1.0;
    private boolean gameOver = false;

    public static final double SPEED_NORMAL = 1.0;
    public static final double SPEED_FAST = 2.0;
    public static final double SPEED_VERY_FAST = 4.0;

    public Game(World world, Company company) {
        if (world == null) throw new IllegalArgumentException("world cannot be null");
        if (company == null) throw new IllegalArgumentException("company cannot be null");
        this.world = world;
        this.company = company;
    }

    public void update(double deltaTime) {
        if (gameOver) return;
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;

        double simDelta = paused ? 0.0 : deltaTime * speedMultiplier;
        if (simDelta <= 0.0) return;

        tick++;
        world.tick(simDelta);
        company.tick(simDelta);

        if (company.isBankrupt()) {
            gameOver = true;
            paused = true;
        }
    }

    public boolean isGameOver() { return gameOver; }
    public long getTick() { return tick; }
    public void setPaused(boolean paused) { this.paused = paused; }
    public boolean isPaused() { return paused; }
    public void setSpeedMultiplier(double speedMultiplier) {
        if (Double.isNaN(speedMultiplier) || speedMultiplier <= 0.0) {
            throw new IllegalArgumentException("speedMultiplier must be > 0");
        }
        this.speedMultiplier = speedMultiplier;
    }
    public double getSpeedMultiplier() { return speedMultiplier; }
}