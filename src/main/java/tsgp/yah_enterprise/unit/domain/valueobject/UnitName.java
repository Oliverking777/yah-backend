package tsgp.yah_enterprise.unit.domain.valueobject;

import tsgp.yah_enterprise.unit.domain.exception.InvalidUnitException;

/**
 * Display name of a unit (for example "Carton"). Trimmed, required, at most 50 characters.
 * Names are unique ignoring case: the repository and a database index enforce that rule.
 */
public record UnitName(String value) {

    public static final int MAX_LENGTH = 50;

    public UnitName {
        if (value == null || value.isBlank()) {
            throw new InvalidUnitException("Unit name is required");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidUnitException("Unit name must not exceed " + MAX_LENGTH + " characters");
        }
    }

    public static UnitName of(String value) {
        return new UnitName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
