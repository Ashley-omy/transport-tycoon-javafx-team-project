package model;

import common.Id;

public abstract class Facility extends MapEntity {
    protected final GoodsType inputType;    // consumes
    protected final GoodsType outputType;   // produces
    
    protected int inputStock;
    protected int outputStock;
    protected final int maxStockCapacity;
    
    protected final int productionRate;     // units produced per production cycle
    protected final double productionTime;  // time for one production cycle
    protected double productionProgress;

    public Facility(Id id, GoodsType inputType, GoodsType outputType, int maxStockCapacity, int productionRate, double productionTime) {
        super(id);
        if (outputType == null) {
            throw new IllegalArgumentException("outputType cannot be null");
        }
        if (maxStockCapacity <= 0) {
            throw new IllegalArgumentException("maxStockCapacity must be > 0");
        }
        if (productionRate <= 0) {
            throw new IllegalArgumentException("productionRate must be > 0");
        }
        if (productionTime <= 0) {
            throw new IllegalArgumentException("productionTime must be > 0");
        }
        
        this.inputType = inputType;
        this.outputType = outputType;
        this.maxStockCapacity = maxStockCapacity;
        this.productionRate = productionRate;
        this.productionTime = productionTime;
        this.inputStock = 0;
        this.outputStock = 0;
        this.productionProgress = 0.0;
    }

    public GoodsType getInputType() {
        return inputType;
    }

    public GoodsType getOutputType() {
        return outputType;
    }

    public int getInputStock() {
        return inputStock;
    }

    public int getOutputStock() {
        return outputStock;
    }

    public int getMaxStockCapacity() {
        return maxStockCapacity;
    }

    public boolean needsInput() {
        return inputType != null && inputStock < maxStockCapacity;
    }

    public boolean hasOutput() {
        return outputStock > 0;
    }


    @Override
    public void acceptDelivery(Shipment shipment) {
        if (shipment == null) return;
        if (!shipment.isGoods()) return;
        if (inputType == null) return; // facility doesn't consume anything
        if (shipment.getGoodsType() != inputType) return; // wrong goods type

        int space = maxStockCapacity - inputStock;
        int toAccept = Math.min(shipment.getUnits(), space);
        
        if (toAccept > 0) {
            inputStock += toAccept;
        }
    }

    /**
     * production logic: convert input to output over time.
     */
    @Override
    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;

        // can only produce if we have space for output
        if (outputStock >= maxStockCapacity) return;

        // if we need input, check if we have enough
        if (inputType != null && inputStock < productionRate) return;

        productionProgress += deltaTime;

        if (productionProgress >= productionTime) {
            // Production cycle complete
            productionProgress -= productionTime;

            // Consume input if needed
            if (inputType != null) {
                inputStock -= productionRate;
            }

            // Produce output
            int spaceForOutput = maxStockCapacity - outputStock;
            int produced = Math.min(productionRate, spaceForOutput);
            outputStock += produced;
        }
    }

    /**
     * Emit produced goods to attached stops.
     */
    @Override
    public void emitSupplyToStops() {
        if (outputStock <= 0 || servedStops.isEmpty()) return;

        // Distribute output evenly to all stops
        int perStop = outputStock / servedStops.size();
        if (perStop <= 0) return;

        for (Stop stop : servedStops) {
            if (outputStock <= 0) break;

            int toEmit = Math.min(perStop, outputStock);
            
            // Create shipment from this facility to the stop
            Shipment shipment = new Shipment(
                ShipmentKind.GOODS,
                outputType,
                toEmit,
                stop.getId(),  // from this stop
                stop.getId(),  // to same stop (placeholder - actual destination determined by route)
                common.Money.of(1)  // value per unit (1 coin per unit)
            );

            stop.enqueue(shipment);
            outputStock -= toEmit;
        }
    }
}
