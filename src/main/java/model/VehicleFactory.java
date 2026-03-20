package model;

import common.Id;
import common.Money;

/**
 * Factory class to create different vehicle types with predefined costs and speeds.
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
            Money.of(1),           // maintenance: 1 coin/tick
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
            Money.of(4),           // maintenance: 4 coins/tick
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
            Money.of(1),           // maintenance: 1 coin/tick
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
            Money.of(3),           // maintenance: 3 coins/tick
            1.5                    // speed: 1.5 tiles/second
        );
    }
}
