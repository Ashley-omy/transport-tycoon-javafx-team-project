package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void constructorShouldStoreFieldsAndSetWorldOnCompany() {
        World world = new World(30, 30);
        Company company = new Company();
        Game game = new Game(world, company);
        
        assertEquals(world, game.getWorld());
        assertEquals(company, game.getCompany());
    }

    @Test
    void constructorShouldThrowOnNullWorld() {
        assertThrows(IllegalArgumentException.class, () -> new Game(null, new Company()));
    }

    @Test
    void constructorShouldThrowOnNullCompany() {
        assertThrows(IllegalArgumentException.class, () -> new Game(new World(30, 30), null));
    }

    @Test
    void updateShouldIncrementTimeAndTick() {
        World world = new World(30, 30);
        Company company = new Company();
        Game game = new Game(world, company);
        
        assertEquals(0L, game.getTick());
        assertEquals(0.0, game.getElapsedTimeSeconds());
        
        game.update(1.5);
        
        assertEquals(1L, game.getTick());
        assertEquals(1.5, game.getSimDelta());
        assertEquals(1.5, game.getElapsedTimeSeconds());
        assertEquals("00:00:01", game.getFormattedTime());
    }

    @Test
    void updateShouldIgnoreInvalidDeltaTime() {
        World world = new World(30, 30);
        Company company = new Company();
        Game game = new Game(world, company);
        
        game.update(0.0);
        assertEquals(0L, game.getTick());
        
        game.update(-1.0);
        assertEquals(0L, game.getTick());
        
        game.update(Double.NaN);
        assertEquals(0L, game.getTick());
    }

    @Test
    void updateShouldSetGameOverWhenCompanyBankrupt() {
        World world = new World(30, 30);
        Company company = new Company();
        // Force bankruptcy by spending too much money.
        company.getEconomy().forceSubtract(common.Money.of(200000), TransactionType.OTHER_EXPENSE, "Loss");
        
        Game game = new Game(world, company);
        game.update(1.0);
        
        assertTrue(game.isGameOver());
        
        // Further updates shouldn't change tick if game is over
        game.update(1.0);
        assertEquals(1L, game.getTick());
        assertEquals(1.0, game.getElapsedTimeSeconds());
    }
}
