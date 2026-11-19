package tsgp.yah_enterprise.purchasing.domain.valueobject;

import tsgp.yah_enterprise.purchasing.domain.exception.InvalidPurchasePriceException;

/** Whole FCFA, never negative. Purchasing's own copy: modules share ids, not classes. */
public record Money(long amount) implements Comparable<Money> {

    public static final Money ZERO = new Money(0);

    public Money {
        if (amount < 0) {
            throw new InvalidPurchasePriceException("An amount cannot be negative");
        }
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(amount, other.amount));
    }

    public Money times(long factor) {
        return new Money(Math.multiplyExact(amount, factor));
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
