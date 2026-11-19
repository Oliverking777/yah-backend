package tsgp.yah_enterprise.location.domain.model;

import tsgp.yah_enterprise.location.domain.exception.InvalidLocationException;
import tsgp.yah_enterprise.location.domain.exception.LocationAlreadyActiveException;
import tsgp.yah_enterprise.location.domain.exception.LocationAlreadyInactiveException;
import tsgp.yah_enterprise.location.domain.exception.LocationInactiveException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * A physical stock/business location (warehouse or store).
 * Aggregate root of the Location module.
 */

@Getter
public class Location {

    private static final int NAME_MAX = 150;
    private static final int ADDRESS_MAX = 500;

    private final UUID locationId;
    private final LocationCode code;
    private String name;
    private LocationType locationType;
    /** Optional: null when no address was given. */
    private String address;
    private LocationStatus locationStatus;
    private final Instant createdAt;
    private Instant updatedAt;

    private Location(UUID locationId, String name, LocationCode code, LocationType locationType,
                     String address, LocationStatus locationStatus,
                     Instant createdAt, Instant updatedAt) {
        this.locationId = Objects.requireNonNull(locationId, "locationId");
        this.code = Objects.requireNonNull(code, "code");
        this.name = validateName(name);
        this.locationType = requireType(locationType);
        this.address = validateAddress(address);
        this.locationStatus = Objects.requireNonNull(locationStatus, "locationStatus");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    /** Creates a brand-new location. New locations are ACTIVE. */
    public static Location create(String name, LocationCode code, LocationType type, String address) {
        Instant now = Instant.now();
        return new Location(UUID.randomUUID(), name, code, type, address,
                LocationStatus.ACTIVE, now, now);
    }

    /** Rebuilds a location from persistence. No business events, no timestamp changes. */
    public static Location reconstitute(UUID locationId, String name, LocationCode code,
                                        LocationType type, String address, LocationStatus status,
                                        Instant createdAt, Instant updatedAt) {
        return new Location(locationId, name, code, type, address, status, createdAt, updatedAt);
    }

    // ---- behaviour ----

    /**
     * Updates descriptive details. Code is immutable; status changes go through activate/deactivate.
     * A null or blank address removes it.
     */
    public void updateDetails(String name, LocationType type, String address) {
        this.name = validateName(name);
        this.locationType = requireType(type);
        this.address = validateAddress(address);
        touch();
    }

    public void activate() {
        if (locationStatus == LocationStatus.ACTIVE) {
            throw new LocationAlreadyActiveException(locationId);
        }
        this.locationStatus = LocationStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        if (locationStatus == LocationStatus.INACTIVE) {
            throw new LocationAlreadyInactiveException(locationId);
        }
        this.locationStatus = LocationStatus.INACTIVE;
        touch();
    }

    public boolean isActive() {
        return locationStatus == LocationStatus.ACTIVE;
    }

    /** Guard used by other use cases (user assignment, stock movements, sales...). */
    public void ensureActive() {
        if (!isActive()) {
            throw new LocationInactiveException(locationId);
        }
    }


    // ---- internals ----

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidLocationException("Location name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() > NAME_MAX) {
            throw new InvalidLocationException("Location name must not exceed " + NAME_MAX + " characters");
        }
        return trimmed;
    }

    /** The address is optional: null, empty or blank means "no address". */
    private static String validateAddress(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        String trimmed = address.trim();
        if (trimmed.length() > ADDRESS_MAX) {
            throw new InvalidLocationException("Location address must not exceed " + ADDRESS_MAX + " characters");
        }
        return trimmed;
    }

    private static LocationType requireType(LocationType type) {
        if (type == null) {
            throw new InvalidLocationException("Location type is required");
        }
        return type;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Location other && locationId.equals(other.locationId));
    }

    @Override
    public int hashCode() {
        return locationId.hashCode();
    }
}