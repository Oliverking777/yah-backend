package tsgp.yah_enterprise.iam.domain.model;

import tsgp.yah_enterprise.iam.domain.exception.InvalidLocationScopeException;

/**
 * Where the users of a role work.
 * <ul>
 *   <li>ALL: across every location, so they need no location assignment (e.g. the owner).</li>
 *   <li>ASSIGNED: they must belong to exactly one location (e.g. managers and employees).</li>
 * </ul>
 * This is data on the role, so no code has to check role names.
 */
public enum LocationScope {
    ALL,
    ASSIGNED;

    public boolean requiresLocation() {
        return this == ASSIGNED;
    }

    public static LocationScope from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidLocationScopeException(value);
        }
        try {
            return LocationScope.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidLocationScopeException(value);
        }
    }
}