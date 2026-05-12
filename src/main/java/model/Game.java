package model;

public class Game implements java.io.Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final World world;
    private final Company company;

    private long tick = 0L;
    private boolean gameOver = false;
    private double simDelta;
    private double elapsedTimeSeconds = 0.0;

    public Game(World world, Company company) {
        if (world == null) throw new IllegalArgumentException("world cannot be null");
        if (company == null) throw new IllegalArgumentException("company cannot be null");
        this.world = world;
        this.company = company;
        company.setWorld(world);
    }

    public void update(double deltaTime) {
        simDelta = 0.0;
        if (gameOver) return;
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;

        simDelta = deltaTime;

        tick++;
        elapsedTimeSeconds += simDelta;
        world.tick(simDelta);
        company.tick(simDelta);

        if (company.isBankrupt()) {
            gameOver = true;
        }
    }

    public World getWorld() {
        return world;
    }
    public Company getCompany() {
        return company;
    }

    public boolean isGameOver() { return gameOver; }
    public long getTick() { return tick; }
    public double getSimDelta() { return simDelta; }
    public double getElapsedTimeSeconds() { return elapsedTimeSeconds; }
    public String getFormattedTime() {
        int totalSeconds = (int) elapsedTimeSeconds;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
