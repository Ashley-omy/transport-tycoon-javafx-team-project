package model;

import common.Id;
import common.Money;

/**
 * Factory class to create different vehicle types with predefined costs and speeds.
 * Maintenance costs are deducted every 30 seconds.
 */
public class VehicleFactory {
    
    /**
     * Small truck: Low capacity, cheap, fast
     */
    public static Truck createSmallTruck(Id id) {
        return new Truck(
            id,
            50,                    // capacity: 50 units
            Money.of(3_000),       // purchase: 3,000 coins
            Money.of(5),           // maintenance: 5 coins per 30 seconds
            2.0                    // speed: 2.0 tiles/second
        );
    }
    
    /**
     * Large truck: High capacity, expensive, slow
     */
    public static Truck createLargeTruck(Id id) {
        return new Truck(
            id,
            150,                   // capacity: 150 units
            Money.of(8_000),       // purchase: 8,000 coins
            Money.of(20),          // maintenance: 20 coins per 30 seconds
            1.0                    // speed: 1.0 tiles/second
        );
    }
    
    /**
     * Small bus: Low capacity, cheap, fast
     */
    public static Bus createSmallBus(Id id) {
        return new Bus(
            id,
            30,                    // capacity: 30 passengers
            Money.of(2_000),       // purchase: 2,000 coins
            Money.of(4),           // maintenance: 4 coins per 30 seconds
            2.5                    // speed: 2.5 tiles/second
        );
    }
    
    /**
     * Large bus: High capacity, expensive, slower
     */
    public static Bus createLargeBus(Id id) {
        return new Bus(
            id,
            80,                    // capacity: 80 passengers
            Money.of(5_000),       // purchase: 5,000 coins
            Money.of(15),          // maintenance: 15 coins per 30 seconds
            1.5                    // speed: 1.5 tiles/second
        );
    }
}
