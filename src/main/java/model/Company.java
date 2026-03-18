package model;

import common.Money;

import java.util.ArrayList;
import java.util.List;

public class Company {
    private final Economy economy;
    private final List<Vehicle> fleet = new ArrayList<>();

    public static final Money DEFAULT_STARTING_CAPITAL = Money.of(100_000);

    // temporary fixed costs until vehicle and shipment classes are implemented
    private static final Money DEFAULT_VEHICLE_PURCHASE_COST = Money.of(5_000);
    private static final Money DEFAULT_VEHICLE_RESALE_VALUE = Money.of(2_500);
    private static final Money MAINTENANCE_COST_PER_VEHICLE_PER_TICK = Money.of(2);
    private static final Money DEFAULT_DELIVERY_INCOME = Money.of(300);

    public Company() {
        this(DEFAULT_STARTING_CAPITAL);
    }

    public Company(Money startingCapital) {
        if (startingCapital == null) throw new IllegalArgumentException("startingCapital cannot be null");
        this.economy = new Economy(startingCapital);
    }

    public Economy getEconomy() {
        return economy;
    }

    public List<Vehicle> getFleet() {
        return fleet;
    }

    // vehicle purchase cost
    public boolean buyVehicle(Vehicle v) {
        if (v == null) throw new IllegalArgumentException("vehicle cannot be null");
        if (!economy.spend(DEFAULT_VEHICLE_PURCHASE_COST)) return false;
        fleet.add(v);
        return true;
    }

    public void sellVehicle(Vehicle v) {
        if (v == null) return;
        if (fleet.remove(v)) {
            economy.earn(DEFAULT_VEHICLE_RESALE_VALUE);
        }
    }

    // income from deliveries
    public void completeShipment(Shipment s) {
        if (s == null) return;
        economy.earn(DEFAULT_DELIVERY_INCOME);
    }

    public void earn(Money amount) {
        economy.earn(amount);
    }

    public boolean spend(Money amount) {
        return economy.spend(amount);
    }

    // bankruptcy
    public boolean isBankrupt() {
        return economy.isBankrupt();
    }

    // maintenance cost
    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || deltaTime <= 0.0) return;

        // charge maintenance every tick per vehicle
        for (int i = 0; i < fleet.size(); i++) {
            boolean paid = economy.spend(MAINTENANCE_COST_PER_VEHICLE_PER_TICK);
            if (!paid) {
                // force negative balance to trigger bankruptcy
                economy.forceSubtract(MAINTENANCE_COST_PER_VEHICLE_PER_TICK);
            }
        }
    }
}