package model;

import common.Money;
import java.util.ArrayList;
import java.util.List;

public class Economy {
    private Money cash;
    private final List<Transaction> transactionHistory = new ArrayList<>();
    private Money totalEarned = Money.ZERO;
    private Money totalSpent = Money.ZERO;

    public Economy(Money startingCash) {
        if (startingCash == null) throw new IllegalArgumentException("startingCash cannot be null");
        this.cash = startingCash;
        if (startingCash.isPositive()) {
            recordTransaction(TransactionType.INITIAL_CAPITAL, startingCash, "Starting capital");
        }
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
        totalSpent = totalSpent.add(cost);
        return true;
    }

    public boolean spend(Money cost, TransactionType type, String description) {
        if (spend(cost)) {
            recordTransaction(type, cost.negate(), description);
            return true;
        }
        return false;
    }

    // earn money logic
    public void earn(Money amount) {
        validateMoney(amount);
        cash = cash.add(amount);
        totalEarned = totalEarned.add(amount);
    }

    public void earn(Money amount, TransactionType type, String description) {
        earn(amount);
        recordTransaction(type, amount, description);
    }

    // when rule requires a payment even if not affordable
    public void forceSubtract(Money cost) {
        validateMoney(cost);
        cash = cash.subtract(cost); // can become negative
        totalSpent = totalSpent.add(cost);
    }

    public void forceSubtract(Money cost, TransactionType type, String description) {
        forceSubtract(cost);
        recordTransaction(type, cost.negate(), description);
    }

    // bankruptcy
    public boolean isBankrupt() {
        return cash.isNegative();
    }

    private void validateMoney(Money m) {
        if (m == null) throw new IllegalArgumentException("money cannot be null");
        if (m.isNegative()) throw new IllegalArgumentException("negative money not allowed");
    }

    private void recordTransaction(TransactionType type, Money amount, String description) {
        transactionHistory.add(new Transaction(type, amount, description, cash));
    }

    public static class Transaction {
        private final TransactionType type;
        private final Money amount;
        private final String description;
        private final Money balanceAfter;
        private final long timestamp;

        public Transaction(TransactionType type, Money amount, String description, Money balanceAfter) {
            this.type = type;
            this.amount = amount;
            this.description = description;
            this.balanceAfter = balanceAfter;
            this.timestamp = System.currentTimeMillis();
        }


        @Override
        public String toString() {
            return String.format("%s: %s - %s (Balance: %s)",
                    type, amount, description, balanceAfter);
        }
    }
}