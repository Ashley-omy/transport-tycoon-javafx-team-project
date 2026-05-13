package model;

import common.Money;

import java.util.ArrayList;
import java.util.List;

public class Company implements java.io.Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    
    private final Economy economy;
    private final List<Vehicle> fleet = new ArrayList<>();
    private World world;

    public static final Money DEFAULT_STARTING_CAPITAL = Money.of(100_000);

    private static final Money DEFAULT_VEHICLE_RESALE_VALUE = Money.of(2_500);
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

        Garage homeGarage = v.getHomeGarage();
        if (homeGarage != null) {
            homeGarage.sellVehicle(v);
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

    // income from deliveries with actual payout
    public void completeShipmentWithPayout(Money payout) {
        if (payout == null || payout.isNegative()) return;
        economy.earn(payout, TransactionType.DELIVERY_INCOME, "Delivered shipment");
    }

    // have to deal with maintenance from garage
    public void performVehicleMaintenance(Vehicle vehicle) {
        if (vehicle == null) return;

        Money cost = vehicle.getMaintenanceCost();
        boolean paid = economy.spend(
                cost,
                TransactionType.VEHICLE_MAINTENANCE,
                "Maintenance for vehicle " + vehicle.getId()
        );

        if (world != null) {
            String maintenanceText = "-" + cost.amount();
            world.pushCostMessage("Maintenance fee: " + maintenanceText);
            Garage homeGarage = vehicle.getHomeGarage();
            if (homeGarage != null) {
                homeGarage.pushEventDisplay(maintenanceText);
            }
        }

        if (!paid) {
            economy.forceSubtract(
                    cost,
                    TransactionType.VEHICLE_MAINTENANCE,
                    "Forced maintenance for vehicle " + vehicle.getId()
            );
        }
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
    }
}
