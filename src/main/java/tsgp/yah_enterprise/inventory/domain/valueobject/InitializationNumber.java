package tsgp.yah_enterprise.inventory.domain.valueobject;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockInitializationException;

import java.util.regex.Pattern;

/** Human number of a stock initialization, INIT-000001. Assigned by the system from a sequence, never edited. */
public record InitializationNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("^INIT-[0-9]{6,}$");

    public InitializationNumber {
        if (value == null || !FORMAT.matcher(value.trim()).matches()) {
            throw new InvalidStockInitializationException("Initialization number must look like INIT-000001");
        }
        value = value.trim();
    }

    public static InitializationNumber of(String value) { return new InitializationNumber(value); }

    public static InitializationNumber fromSequence(long sequenceValue) {
        return new InitializationNumber(String.format("INIT-%06d", sequenceValue));
    }

    @Override public String toString() { return value; }
}
