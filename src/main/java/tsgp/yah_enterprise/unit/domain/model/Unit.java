package tsgp.yah_enterprise.unit.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import tsgp.yah_enterprise.unit.domain.exception.UnitAlreadyActiveException;
import tsgp.yah_enterprise.unit.domain.exception.UnitAlreadyInactiveException;
import tsgp.yah_enterprise.unit.domain.exception.UnitInactiveException;
import tsgp.yah_enterprise.unit.domain.valueobject.UnitCode;
import tsgp.yah_enterprise.unit.domain.valueobject.UnitName;

/**
 * A unit of measure (Piece, Dozen, Carton...). Aggregate root of the Unit module.
 * The catalogue is global: it knows nothing about products. Products reference a unit by id only.
 * The code never changes, and a unit is never deleted because products may reference it.
 */
@Getter
public class Unit {

    private final UUID unitId;
    private UnitName unitName;
    private final UnitCode unitCode;
    private UnitStatus unitStatus;
    private final Instant createdAt;
    private Instant updatedAt;

    private Unit(UUID unitId, UnitName unitName, UnitCode unitCode, UnitStatus unitStatus,
                 Instant createdAt, Instant updatedAt) {
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.unitName = Objects.requireNonNull(unitName, "unitName must not be null");
        this.unitCode = Objects.requireNonNull(unitCode, "unitCode must not be null");
        this.unitStatus = Objects.requireNonNull(unitStatus, "unitStatus must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /** Creates a brand new, active unit with a freshly generated id. */
    public static Unit create(String unitName, String unitCode) {
        Instant now = Instant.now();
        return new Unit(UUID.randomUUID(), UnitName.of(unitName), UnitCode.of(unitCode),
                UnitStatus.ACTIVE, now, now);
    }

    /** Rebuilds an existing unit from stored data (for example from the database). */
    public static Unit reconstitute(UUID unitId, String unitName, String unitCode, UnitStatus unitStatus,
                                    Instant createdAt, Instant updatedAt) {
        return new Unit(unitId, UnitName.of(unitName), UnitCode.of(unitCode), unitStatus, createdAt, updatedAt);
    }

    // ---- behaviour ----

    public void rename(String newName) {
        this.unitName = UnitName.of(newName);
        touch();
    }

    public void activate() {
        if (unitStatus == UnitStatus.ACTIVE) {
            throw new UnitAlreadyActiveException(unitId);
        }
        this.unitStatus = UnitStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        if (unitStatus == UnitStatus.INACTIVE) {
            throw new UnitAlreadyInactiveException(unitId);
        }
        this.unitStatus = UnitStatus.INACTIVE;
        touch();
    }

    public boolean isActive() {
        return unitStatus == UnitStatus.ACTIVE;
    }

    /** @throws UnitInactiveException if the unit is not active */
    public void ensureActive() {
        if (!isActive()) {
            throw new UnitInactiveException(unitId);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Unit other && unitId.equals(other.unitId));
    }

    @Override
    public int hashCode() {
        return unitId.hashCode();
    }

    @Override
    public String toString() {
        return "Unit{unitId=" + unitId + ", unitCode=" + unitCode + ", unitName=" + unitName + "}";
    }
}
