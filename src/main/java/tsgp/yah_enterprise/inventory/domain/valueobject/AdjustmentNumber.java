package tsgp.yah_enterprise.inventory.domain.valueobject;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockAdjustmentException;

import java.util.regex.Pattern;

/** Human number of a stock adjustment, ADJ-000001. Assigned by the system from a sequence, never edited. */
public record AdjustmentNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("^ADJ-[0-9]{6,}$");

    public AdjustmentNumber {
        if (value == null || !FORMAT.matcher(value.trim()).matches()) {
            throw new InvalidStockAdjustmentException("Adjustment number must look like ADJ-000001");
        }
        value = value.trim();
    }

    public static AdjustmentNumber of(String value) { return new AdjustmentNumber(value); }

    public static AdjustmentNumber fromSequence(long sequenceValue) {
        return new AdjustmentNumber(String.format("ADJ-%06d", sequenceValue));
    }

    @Override public String toString() { return value; }
}
