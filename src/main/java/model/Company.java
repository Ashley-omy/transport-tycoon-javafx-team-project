package model;

import common.Money;

import java.util.ArrayList;
import java.util.List;

public class Company {
    private final Economy economy;
    private final List<Vehicle> fleet = new ArrayList<>();

    public static final Money DEFAULT_STARTING_CAPITAL = Money.of(100_000);

    // Note: Vehicle costs are now per-vehicle (stored in Vehicle class)
    // These are kept for backward compatibility
    private static final Money DEFAULT_VEHICLE_RESALE_VALUE = Money.of(2_500);
    private static final Money DEFAULT_DELIVERY_INCOME = Money.of(300);

    // getters for cost constants (useful for UI)
    public static Money getVehicleResaleValue() {
        return DEFAULT_VEHICLE_RESALE_VALUE;
    }

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
        if (!economy.spend(v.getPurchaseCost(), TransactionType.VEHICLE_PURCHASE, 
                          "Purchased vehicle " + v.getId())) {
            return false;
        }
        fleet.add(v);
        return true;
    }

    public void sellVehicle(Vehicle v) {
        if (v == null) return;
        if (fleet.remove(v)) {
            economy.earn(DEFAULT_VEHICLE_RESALE_VALUE, TransactionType.VEHICLE_SALE, 
                        "Sold vehicle " + v.getId());
        }
    }

    // income from deliveries
    public void completeShipment(Shipment s) {
        if (s == null) return;
        // Use fixed income for now, but could calculate based on shipment value
        Money income = DEFAULT_DELIVERY_INCOME;
        economy.earn(income, TransactionType.DELIVERY_INCOME, 
                    "Delivered shipment from " + s.getFromStopId() + " to " + s.getToStopId());
    }

    // income from deliveries with actual payout
    public void completeShipmentWithPayout(Money payout) {
        if (payout == null || payout.isNegative()) return;
        economy.earn(payout, TransactionType.DELIVERY_INCOME, "Delivered shipment");
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

        // charge maintenance every tick per vehicle (vehicle-specific cost)
        for (Vehicle v : fleet) {
            Money cost = v.getMaintenanceCost();
            boolean paid = economy.spend(cost, 
                                        TransactionType.VEHICLE_MAINTENANCE, 
                                        "Maintenance for vehicle " + v.getId());
            if (!paid) {
                // force negative balance to trigger bankruptcy
                economy.forceSubtract(cost, 
                                     TransactionType.VEHICLE_MAINTENANCE, 
                                     "Forced maintenance for vehicle " + v.getId());
            }
        }
    }
}