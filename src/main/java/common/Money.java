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

    public boolean isPositive() {
        return amount > 0;
    }

    public Money negate() {
        return new Money(-this.amount, this.currency);
    }

    public Money abs() {
        return amount < 0 ? new Money(-amount, currency) : this;
    }

    public boolean greaterOrEqual(Money other) {
        requireSameCurrency(other);
        return this.amount >= other.amount;
    }

    public boolean greaterThan(Money other) {
        requireSameCurrency(other);
        return this.amount > other.amount;
    }

    public boolean lessThan(Money other) {
        requireSameCurrency(other);
        return this.amount < other.amount;
    }

    public boolean lessOrEqual(Money other) {
        requireSameCurrency(other);
        return this.amount <= other.amount;
    }

    public static Money max(Money a, Money b) {
        if (a == null) return b;
        if (b == null) return a;
        a.requireSameCurrency(b);
        return a.amount >= b.amount ? a : b;
    }

    public static Money min(Money a, Money b) {
        if (a == null) return b;
        if (b == null) return a;
        a.requireSameCurrency(b);
        return a.amount <= b.amount ? a : b;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount == money.amount && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount  + " " +  currency;
    }
}