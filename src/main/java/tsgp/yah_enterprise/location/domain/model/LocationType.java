package tsgp.yah_enterprise.location.domain.model;

import tsgp.yah_enterprise.location.domain.exception.InvalidLocationTypeException;

public enum LocationType {
    WAREHOUSE,
    STORE;

    public static LocationType from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidLocationTypeException(value);
        }
        try {
            return LocationType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidLocationTypeException(value);
        }
    }
}