package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForestTest {

    @Test
    void growsOneTreePerGrowthInterval() {
        Forest forest = new Forest();

        forest.grow(Forest.GROWTH_INTERVAL_SECONDS - 0.1);
        assertEquals(1, forest.getTrees());

        forest.grow(0.1);
        assertEquals(2, forest.getTrees());
    }

    @Test
    void capsTreeGrowthAtFourTrees() {
        Forest forest = new Forest();

        forest.grow(Forest.GROWTH_INTERVAL_SECONDS * 10);

        assertEquals(4, forest.getTrees());
    }

    @Test
    void spreadAttemptsAreThrottledUntilForestIsFullyGrown() {
        Forest forest = new Forest();

        forest.grow(Forest.GROWTH_INTERVAL_SECONDS * 3);
        assertEquals(4, forest.getTrees());

        assertEquals(0, forest.consumeSpreadAttempts(Forest.SPREAD_INTERVAL_SECONDS - 0.1));
        assertEquals(1, forest.consumeSpreadAttempts(0.1));
        assertEquals(1, forest.consumeSpreadAttempts(Forest.SPREAD_INTERVAL_SECONDS));
    }
}
