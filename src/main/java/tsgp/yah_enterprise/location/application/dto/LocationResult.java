package tsgp.yah_enterprise.location.application.dto;

import tsgp.yah_enterprise.location.domain.model.Location;

import java.time.Instant;
import java.util.UUID;

/**
 * Output describing a single location.
 */
public record LocationResult(
    UUID locationId,
    String code,
    String name,
    String locationType,
    String address,
    String locationStatus,
    Instant createdAt,
    Instant updatedAt
) {
    public static LocationResult from(Location location) {
        return new LocationResult(
                location.getLocationId(),
                location.getCode().value(),
                location.getName(),
                location.getLocationType().toString(),
                location.getAddress(),
                location.getLocationStatus().toString(),
                location.getCreatedAt(),
                location.getUpdatedAt());
    }
}