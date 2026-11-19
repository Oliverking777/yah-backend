package tsgp.yah_enterprise.sales.domain.valueobject;

import tsgp.yah_enterprise.sales.domain.exception.InvalidOrderException;

/** Generated from a sequence (ORD-000001), never edited. */
public record OrderNumber(String value) {

    public OrderNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidOrderException("OrderNumber is required");
        }
    }

    public static OrderNumber of(String value) {
        return new OrderNumber(value);
    }

    public static OrderNumber fromSequence(long seq) {
        return new OrderNumber("ORD-" + String.format("%06d", seq));
    }

    @Override
    public String toString() {
        return value;
    }
}
