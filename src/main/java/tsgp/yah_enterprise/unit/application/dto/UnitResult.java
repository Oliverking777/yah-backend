package tsgp.yah_enterprise.unit.application.dto;

import java.time.Instant;
import java.util.UUID;

import tsgp.yah_enterprise.unit.domain.model.Unit;

/** Output describing a single unit. */
public record UnitResult(
        UUID unitId,
        String unitName,
        String unitCode,
        String unitStatus,
        Instant createdAt,
        Instant updatedAt
) {
    public static UnitResult from(Unit unit) {
        return new UnitResult(
                unit.getUnitId(),
                unit.getUnitName().value(),
                unit.getUnitCode().value(),
                unit.getUnitStatus().toString(),
                unit.getCreatedAt(),
                unit.getUpdatedAt());
    }
}
