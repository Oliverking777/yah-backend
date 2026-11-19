package tsgp.yah_enterprise.location.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.location.application.dto.LocationResult;

/**
 * HTTP response body describing one location.
 */
@Schema(description = "A warehouse or a store")
public record LocationResponse(

        @Schema(description = "Id of the location", example = "0b8f2f0e-6a43-4a57-9d38-2f7a1e6c9d10")
        UUID locationId,

        @Schema(description = "Unique code", example = "WH-MAIN")
        String code,

        @Schema(description = "Display name", example = "Main Warehouse")
        String name,

        @Schema(description = "Kind of location", example = "WAREHOUSE", allowableValues = {"WAREHOUSE", "STORE"})
        String locationType,

        @Schema(description = "Physical address", example = "Rue de la Joie, Bonapriso, Douala")
        String address,

        @Schema(description = "Whether the location is operational", example = "ACTIVE",
                allowableValues = {"ACTIVE", "INACTIVE"})
        String locationStatus,

        @Schema(description = "When the location was created (UTC)", example = "2026-10-03T09:15:30Z")
        Instant createdAt,

        @Schema(description = "When the location was last changed (UTC)", example = "2026-10-03T09:15:30Z")
        Instant updatedAt
) {
    public static LocationResponse from(LocationResult result) {
        return new LocationResponse(
                result.locationId(),
                result.code(),
                result.name(),
                result.locationType(),
                result.address(),
                result.locationStatus(),
                result.createdAt(),
                result.updatedAt());
    }
}