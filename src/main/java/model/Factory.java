package model;

import common.Id;

/**
 * Factory consumes raw materials and produces finished goods.
 */
public class Factory extends Facility {
    @java.io.Serial
    private static final long serialVersionUID = -2851907447855130154L;

    public Factory(Id id, GoodsType inputType, GoodsType outputType, int maxStockCapacity, int productionRate, double productionTime) {
        super(id, inputType, outputType, maxStockCapacity, productionRate, productionTime);
        if (inputType == null) {
            throw new IllegalArgumentException("Factory must have an input type");
        }
    }

    /**
     * Create a steel mill (IRON → STEEL).
     */
    public static Factory createSteelMill(Id id) {
        return new Factory(
            id,
            GoodsType.IRON,     // consumes iron
            GoodsType.STEEL,    // produces steel
            150,                // max stock: 150 units
            10,                 // produces 10 units per cycle (consumes 10 iron)
            6.0                 // production time: 6 seconds
        );
    }

    /**
     * Create a paper mill (WOOD → PAPER).
     */
    public static Factory createPaperMill(Id id) {
        return new Factory(
            id,
            GoodsType.WOOD,     // consumes wood
            GoodsType.PAPER,    // produces paper
            120,                // max stock: 120 units
            8,                  // produces 8 units per cycle (consumes 8 wood)
            5.0                 // production time: 5 seconds
        );
    }
}
