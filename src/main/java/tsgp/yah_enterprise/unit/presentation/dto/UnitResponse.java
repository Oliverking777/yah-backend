package tsgp.yah_enterprise.unit.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.unit.application.dto.UnitResult;

/** HTTP response body describing one unit. */
@Schema(description = "A unit of measure")
public record UnitResponse(

        @Schema(description = "Id of the unit", example = "3f1c7a52-9d0e-4b6a-8c21-5e7d9a0b1c34")
        UUID unitId,

        @Schema(description = "Unit name", example = "Carton")
        String unitName,

        @Schema(description = "Unit code", example = "CTN")
        String unitCode,

        @Schema(description = "Whether the unit can be newly assigned to products", example = "ACTIVE",
                allowableValues = {"ACTIVE", "INACTIVE"})
        String unitStatus,

        @Schema(description = "When the unit was created (UTC)", example = "2026-10-13T09:15:30Z")
        Instant createdAt,

        @Schema(description = "When the unit was last changed (UTC)", example = "2026-10-13T09:15:30Z")
        Instant updatedAt
) {
    public static UnitResponse from(UnitResult result) {
        return new UnitResponse(
                result.unitId(),
                result.unitName(),
                result.unitCode(),
                result.unitStatus(),
                result.createdAt(),
                result.updatedAt());
    }
}
