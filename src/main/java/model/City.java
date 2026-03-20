package model;

import common.*;
import java.util.HashMap;
import java.util.Map;

/**
 * City consumes goods and passengers.
 * Cities generates passengers.
 */
public class City extends MapEntity {
    private static final int STANDARD_POPULATION = 5000;
    
    private final int population;
    private final Map<GoodsType, Integer> receivedGoods;  // Tracks delivered goods
    private int receivedPassengers;  // Tracks delivered passengers

    public City(Id id) {
        super(id);
        this.population = STANDARD_POPULATION;
        this.receivedGoods = new HashMap<>();
        this.receivedPassengers = 0;
    }

    public int getPopulation() {
        return population;
    }

    public int getReceivedPassengers() {
        return receivedPassengers;
    }

    public int getReceivedGoods(GoodsType type) {
        return receivedGoods.getOrDefault(type, 0);
    }

    /**
     * Accept delivery of goods or passengers.
     */
    @Override
    public void acceptDelivery(Shipment shipment) {
        if (shipment == null) return;

        if (shipment.isPassengers()) {
            receivedPassengers += shipment.getUnits();
        } else if (shipment.isGoods()) {
            GoodsType type = shipment.getGoodsType();
            int current = receivedGoods.getOrDefault(type, 0);
            receivedGoods.put(type, current + shipment.getUnits());
        }
    }

    /**
     * Emit passengers to attached stops.
     */
    @Override
    public void emitSupplyToStops() {
        if (servedStops.isEmpty()) return;
        int passengersToEmit = population / 10; // 1 passenger per 10 population
        if (passengersToEmit <= 0) return;

        // Distribute evenly to all stops
        int perStop = passengersToEmit / servedStops.size();
        if (perStop <= 0) return;

        for (Stop stop : servedStops) {
            Shipment shipment = new Shipment(
                ShipmentKind.PASSENGERS,
                null,  // no goods type for passengers
                perStop,
                stop.getId(),  // from this stop
                stop.getId(),  // to same stop
                Money.of(2)    // value per passenger (2 coins per passenger)
            );

            stop.enqueue(shipment);
        }
    }

    @Override
    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;
        // will add: demand logic, consumption, growth
    }
}

