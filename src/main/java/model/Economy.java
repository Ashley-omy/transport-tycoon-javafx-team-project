package model;

import common.Money;

public class Economy {
    private Money cash;

    public Economy(Money startingCash) {
        if (startingCash == null) throw new IllegalArgumentException("startingCash cannot be null");
        this.cash = startingCash;
    }

    public Money getCash() {
        return cash;
    }

    public boolean canAfford(Money cost) {
        validateMoney(cost);
        return cash.greaterOrEqual(cost);
    }

    // spend money logic
    public boolean spend(Money cost) {
        validateMoney(cost);
        if (!canAfford(cost)) return false;
        cash = cash.subtract(cost);
        return true;
    }

    // earn money logic
    public void earn(Money amount) {
        validateMoney(amount);
        cash = cash.add(amount);
    }

    // when rule requires a payment even if not affordable
    public void forceSubtract(Money cost) {
        validateMoney(cost);
        cash = cash.subtract(cost); // can become negative
    }

    // bankruptcy
    public boolean isBankrupt() {
        return cash.isNegative();
    }

    private void validateMoney(Money m) {
        if (m == null) throw new IllegalArgumentException("money cannot be null");
        if (m.isNegative()) throw new IllegalArgumentException("negative money not allowed");
    }
}