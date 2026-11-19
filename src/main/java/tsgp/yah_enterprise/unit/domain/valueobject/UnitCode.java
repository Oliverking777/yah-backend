package tsgp.yah_enterprise.unit.domain.valueobject;

import java.util.regex.Pattern;

import tsgp.yah_enterprise.unit.domain.exception.InvalidUnitException;

/**
 * Short code of a unit (for example PCS, DZ, CTN). Trimmed and upper-cased,
 * 1 to 10 letters or digits. Unique, and never changed after the unit is created.
 */
public record UnitCode(String value) {

    public static final int MAX_LENGTH = 10;
    private static final Pattern FORMAT = Pattern.compile("^[A-Z0-9]{1,10}$");

    public UnitCode {
        if (value == null || value.isBlank()) {
            throw new InvalidUnitException("Unit code is required");
        }
        value = value.trim().toUpperCase();
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidUnitException(
                    "Unit code must be 1 to " + MAX_LENGTH + " letters or digits (A-Z, 0-9)");
        }
    }

    public static UnitCode of(String value) {
        return new UnitCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
