package tsgp.yah_enterprise.pricing.domain.valueobject;

import tsgp.yah_enterprise.pricing.domain.exception.InvalidQuantityRangeException;

/** Inclusive quantity interval. A null max means "no upper bound". */
public record QuantityRange(int min, Integer max) {

    public QuantityRange {
        if (min < 1) {
            throw new InvalidQuantityRangeException("Minimum quantity must be at least 1");
        }
        if (max != null && max < min) {
            throw new InvalidQuantityRangeException(
                    "Maximum quantity must not be lower than the minimum quantity");
        }
    }

    public static QuantityRange of(int min, Integer max) {
        return new QuantityRange(min, max);
    }

    /** A flat price: applies from this quantity upwards. */
    public static QuantityRange atLeast(int min) {
        return new QuantityRange(min, null);
    }

    public boolean contains(int quantity) {
        return quantity >= min && quantity <= upperBound();
    }

    /** Touching ranges (1-5 and 5-10) overlap, because both bounds are inclusive. */
    public boolean overlaps(QuantityRange other) {
        return min <= other.upperBound() && other.min <= upperBound();
    }

    private int upperBound() {
        return max == null ? Integer.MAX_VALUE : max;
    }

    @Override
    public String toString() {
        return max == null ? min + "+" : min + "-" + max;
    }
}
