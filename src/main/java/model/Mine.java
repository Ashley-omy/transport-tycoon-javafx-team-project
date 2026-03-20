package model;

import common.Id;

/**
 * Mine produces raw materials
 */
public class Mine extends Facility {

    public Mine(Id id, GoodsType outputType, int maxStockCapacity, int productionRate, double productionTime) {
        super(id, null, outputType, maxStockCapacity, productionRate, productionTime);
        // inputType is null because mines don't need input
    }

    /**
     * Create a standard iron mine.
     */
    public static Mine createIronMine(Id id) {
        return new Mine(
            id,
            GoodsType.IRON,
            200,    // max stock: 200 units
            20,     // produces 20 units per cycle
            5.0     // production time: 5 seconds
        );
    }

    /**
     * Create a standard wood mine.
     */
    public static Mine createWoodMine(Id id) {
        return new Mine(
            id,
            GoodsType.WOOD,
            150,    // max stock: 150 units
            15,     // produces 15 units per cycle
            4.0     // production time: 4 seconds
        );
    }
}
