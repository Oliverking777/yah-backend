package tsgp.yah_enterprise.payment.domain.valueobject;

import tsgp.yah_enterprise.payment.domain.exception.InvalidPaymentAmountException;

/** Whole FCFA, never negative, no decimals (F11). Payment's own copy: modules share ids, not classes. */
public record Money(long amount) implements Comparable<Money> {

    public static final Money ZERO = new Money(0);

    public Money {
        if (amount < 0) {
            throw new InvalidPaymentAmountException("An amount cannot be negative");
        }
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(amount, other.amount));
    }

    /** Fails when the result would be negative. */
    public Money minus(Money other) {
        return new Money(amount - other.amount);
    }

    public boolean isZero() {
        return amount == 0;
    }

    public boolean isPositive() {
        return amount > 0;
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
