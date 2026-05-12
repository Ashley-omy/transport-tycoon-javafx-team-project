package common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {
    @Test
    void currencyShouldBeNormalizedForCaseInsensitiveSaveCompatibility() {
        Money loadedStyleMoney = new Money(100, "Coins");
        Money currentStyleMoney = Money.of(25);

        assertEquals("coins", loadedStyleMoney.currency());
        assertTrue(loadedStyleMoney.greaterOrEqual(currentStyleMoney));
        assertEquals(Money.of(75), loadedStyleMoney.subtract(currentStyleMoney));
    }
}
