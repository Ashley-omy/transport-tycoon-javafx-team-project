package model;

import common.Money;

import java.util.ArrayList;
import java.util.List;

public class Company {
    private final Economy economy;
    private final List<Vehicle> fleet = new ArrayList<>();
    private double maintenanceTimer = 0.0; // Timer for 30-second maintenance intervals
    // Temporary debug hook so maintenance deductions can be shown in the debug window.
    private World world;

    public static final Money DEFAULT_STARTING_CAPITAL = Money.of(100_000);

    private static final Money DEFAULT_VEHICLE_RESALE_VALUE = Money.of(2_500);
    private static final Money DEFAULT_DELIVERY_INCOME = Money.of(300);
    private static final double MAINTENANCE_INTERVAL = 30.0; // Deduct maintenance every 30 seconds

    // getters for cost constants
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

    public void setWorld(World world) {
        this.world = world;
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
        v.setOwner(this); // Set this company as owner
        fleet.add(v);
        return true;
    }

    public void sellVehicle(Vehicle v) {
        if (v == null) return;
        if (fleet.remove(v)) {
            v.setOwner(null); // Remove ownership
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

        for (Vehicle v : fleet) {
            v.tick(deltaTime);
        }

        // Deduct maintenance every 30 seconds
        maintenanceTimer += deltaTime;
        if (maintenanceTimer >= MAINTENANCE_INTERVAL) {
            maintenanceTimer -= MAINTENANCE_INTERVAL;
            
            // Calculate total maintenance cost for all vehicles
            long totalCost = 0;
            for (Vehicle v : fleet) {
                totalCost += v.getMaintenanceCost().amount();
            }
            
            if (totalCost > 0) {
                Money cost = Money.of(totalCost);
                boolean paid = economy.spend(cost, 
                                            TransactionType.VEHICLE_MAINTENANCE, 
                                            "Maintenance for " + fleet.size() + " vehicle(s)");
                if (world != null) {
                    // Temporary debug message for verifying maintenance deductions.
                    world.pushCostMessage("Maintenance fee: -" + cost + " for " + fleet.size() + " vehicle(s)");
                }
                if (!paid) {
                    economy.forceSubtract(cost, 
                                         TransactionType.VEHICLE_MAINTENANCE, 
                                         "Forced maintenance for " + fleet.size() + " vehicle(s)");
                }
            }
        }
    }
}
