package tsgp.yah_enterprise.sales.domain.valueobject;

import tsgp.yah_enterprise.sales.domain.exception.InvalidOrderException;

/** Whole FCFA, never negative. Sales' own copy: modules share ids, not classes. */
public record Money(long amount) implements Comparable<Money> {

    public static final Money ZERO = new Money(0);

    public Money {
        if (amount < 0) {
            throw new InvalidOrderException("An amount cannot be negative");
        }
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(amount, other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount - other.amount);
    }

    public Money times(long factor) {
        return new Money(Math.multiplyExact(amount, factor));
    }

    public boolean isZero() {
        return amount == 0;
    }

    public boolean isGreaterThan(Money other) {
        return amount > other.amount;
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(amount, other.amount);
    }

    @Override
    public String toString() {
        return Long.toString(amount);
    }
}
