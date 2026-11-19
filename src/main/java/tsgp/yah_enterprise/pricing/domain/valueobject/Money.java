package tsgp.yah_enterprise.pricing.domain.valueobject;

import tsgp.yah_enterprise.pricing.domain.exception.InvalidPriceException;

/** An amount in whole FCFA (no decimals). Always strictly positive. */
public record Money(long amount) {

    public Money {
        if (amount <= 0) {
            throw new InvalidPriceException("Price must be greater than 0 FCFA");
        }
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    /** Line total for a quantity; fails loudly instead of overflowing silently. */
    public Money times(int quantity) {
        if (quantity < 1) {
            throw new InvalidPriceException("Quantity must be at least 1");
        }
        try {
            return new Money(Math.multiplyExact(amount, (long) quantity));
        } catch (ArithmeticException e) {
            throw new InvalidPriceException("Line total is too large");
        }
    }
}
