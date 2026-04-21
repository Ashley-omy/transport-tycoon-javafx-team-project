package model;

import common.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EconomyTest {
    private Economy economy;

    @BeforeEach
    void setUp() {
        economy = new Economy(Money.of(1_000));
    }

    @Test
    void earnIncreasesCashByExactAmount() {
        economy.earn(Money.of(250));

        assertEquals(Money.of(1_250), economy.getCash());
    }

    @Test
    void spendReturnsTrueAndDecreasesCash() {
        boolean spent = economy.spend(Money.of(300));

        assertTrue(spent);
        assertEquals(Money.of(700), economy.getCash());
    }

    @Test
    void overspendReturnsFalseAndKeepsCashUnchanged() {
        boolean spent = economy.spend(Money.of(2_000));

        assertFalse(spent);
        assertEquals(Money.of(1_000), economy.getCash());
    }

    @Test
    void forceSubtractCanMakeEconomyBankrupt() {
        economy.forceSubtract(Money.of(1_500));

        assertTrue(economy.getCash().isNegative());
        assertTrue(economy.isBankrupt());
    }

    @Test
    void earnAndSpendRejectNullAndNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> economy.earn(null));
        assertThrows(IllegalArgumentException.class, () -> economy.earn(Money.of(-1)));
        assertThrows(IllegalArgumentException.class, () -> economy.spend(null));
        assertThrows(IllegalArgumentException.class, () -> economy.spend(Money.of(-1)));
    }
}
