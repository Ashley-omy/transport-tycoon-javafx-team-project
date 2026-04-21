package model;

import common.*;
import java.util.HashMap;
import java.util.Map;

/**
 * City consumes goods and passengers.
 * Cities generate passengers and have demands that increase over time.
 */
public class City extends MapEntity {
    private static final int STANDARD_POPULATION = 5000;
    private static final int CITY_FOOTPRINT = 5;
    private static final double DEMAND_INCREASE_RATE = 0.2; // Units per second
    private static final int INITIAL_DEMAND = 50;
    private static final int MAX_DEMAND = 500;
    
    private final int population;
    private final Map<GoodsType, Integer> receivedGoods;  // Tracks delivered goods
    private int receivedPassengers;  // Tracks delivered passengers

    private final Map<GoodsType, Double> goodsDemand;  // Current demand for each goods type
    private double passengerDemand;  // Current demand for passengers

    public City(Id id) {
        super(id, CITY_FOOTPRINT);
        this.population = STANDARD_POPULATION;
        this.receivedGoods = new HashMap<>();
        this.receivedPassengers = 0;
        
        // Initialize demands
        this.goodsDemand = new HashMap<>();
        
        // initial demand for finished products
        goodsDemand.put(GoodsType.STEEL, (double) INITIAL_DEMAND);
        goodsDemand.put(GoodsType.PAPER, (double) INITIAL_DEMAND);
        
        this.passengerDemand = INITIAL_DEMAND;
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
    
    public int getGoodsDemand(GoodsType type) {
        Double demand = goodsDemand.getOrDefault(type, 0.0);
        return demand.intValue();
    }
    
    public int getPassengerDemand() {
        return (int) passengerDemand;
    }

    /**
     * Internal city roads are tile-based crossing lanes:
     * center row and center column of the city footprint.
     * For a 5x5 city, that means row 3 and column 3.
     */
    public boolean hasInternalRoadAt(GridPos pos) {
        if (pos == null || occupiedTiles.isEmpty()) {
            return false;
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Tile tile : occupiedTiles) {
            GridPos tilePos = tile.getPos();
            minX = Math.min(minX, tilePos.x);
            maxX = Math.max(maxX, tilePos.x);
            minY = Math.min(minY, tilePos.y);
            maxY = Math.max(maxY, tilePos.y);
        }

        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;
        return pos.x == centerX || pos.y == centerY;
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
            if (type != GoodsType.STEEL && type != GoodsType.PAPER) {
                return;
            }
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
        
        int passengersToEmit = population / 10; //
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
        int totalEmitted = perStop * servedStops.size();
        if (totalEmitted > 0) {
            // This is generated passenger supply, not demand.
            pushEventDisplay("Supply +" + totalEmitted + " PASSENGERS");
        }
    }

    /**
     * Update demand levels - demands increase slowly over time.
     */
    @Override
    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;
        
        // Increase goods demand gradually
        for (GoodsType type : goodsDemand.keySet()) {
            double current = goodsDemand.get(type);
            
            if (current < MAX_DEMAND) {
                double newDemand = Math.min(current + DEMAND_INCREASE_RATE * deltaTime, MAX_DEMAND);
                goodsDemand.put(type, newDemand);
            }
        }
        
        // Increase passenger demand gradually
        if (passengerDemand < MAX_DEMAND) {
            passengerDemand = Math.min(passengerDemand + DEMAND_INCREASE_RATE * deltaTime, MAX_DEMAND);
        }
    }
}
