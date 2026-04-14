package controller;

import common.GridPos;
import common.Money;
import model.Company;
import model.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildControllerTest {

    @Test
    void buildGarageShouldSucceedOnEmptyTileNextToRoad() {
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos garagePos = new GridPos(8, 5);

        Money cashBefore = company.getEconomy().getCash();

        ActionResult result = buildController.buildGarage(garagePos);

        assertTrue(result.isSuccess());
        assertEquals("Garage built successfully", result.getMessage());
        assertTrue(world.getMap().getTile(garagePos).hasGarage());
        assertEquals(cashBefore.subtract(World.GARAGE_BUILD_COST), company.getEconomy().getCash());
    }

    @Test
    void buildGarageShouldFailWhenTileIsNotAdjacentToRoad() {
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos garagePos = new GridPos(20, 20);

        ActionResult result = buildController.buildGarage(garagePos);

        assertFalse(result.isSuccess());
        assertNull(world.getMap().getTile(garagePos).getGarage());
    }

    @Test
    void buildGarageShouldFailWhenPlayerCannotAffordIt() {
        World world = new World(25, 25);
        Company company = new Company(Money.of(1_000));
        BuildController buildController = new BuildController(world, company);
        GridPos garagePos = new GridPos(8, 5);

        ActionResult result = buildController.buildGarage(garagePos);

        assertFalse(result.isSuccess());
        assertEquals("Not enough money", result.getMessage());
        assertNull(world.getMap().getTile(garagePos).getGarage());
        assertEquals(Money.of(1_000), company.getEconomy().getCash());
    }
}
