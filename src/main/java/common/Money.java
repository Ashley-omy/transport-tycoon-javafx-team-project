package common;

import java.util.Objects;

public final class Money implements Comparable<Money> {
    private final long amount;
    private final String currency;

    public static final String DEFAULT_CURRENCY = "Coins";
    public static final Money ZERO = new Money(0, DEFAULT_CURRENCY);

    public Money(long amount, String currency) {
        this.amount = amount;
        this.currency = (currency == null || currency.isBlank()) ? DEFAULT_CURRENCY : currency;
    }

    public static Money of(long amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public long amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(this.amount, other.amount), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(Math.subtractExact(this.amount, other.amount), this.currency);
    }

    public Money multiply(double factor) {
        if (Double.isNaN(factor) || factor < 0) {
            throw new IllegalArgumentException("factor must be >= 0");
        }
        long result = Math.round(this.amount * factor);
        return new Money(result, this.currency);
    }

    public boolean isNegative() {
        return amount < 0;
    }

    public boolean isZero() {
        return amount == 0;
    }

    public boolean greaterOrEqual(Money other) {
        requireSameCurrency(other);
        return this.amount >= other.amount;
    }

    public boolean lessThan(Money other) {
        requireSameCurrency(other);
        return this.amount < other.amount;
    }

    private void requireSameCurrency(Money other) {
        if (!Objects.equals(this.currency, other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }

    @Override
    public int compareTo(Money o) {
        requireSameCurrency(o);
        return Long.compare(this.amount, o.amount);
    }

    @Override
    public String toString() {
        return currency + " " + amount;
    }
}