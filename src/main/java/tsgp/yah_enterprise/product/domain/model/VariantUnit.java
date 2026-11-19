package tsgp.yah_enterprise.product.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A unit a variant is counted in (Piece, Carton, Bag...). Child of {@link ProductVariant}.
 * conversionFactor: 1 of this unit = N base units of the variant. The base unit has factor 1, the others 2 or more.
 * Replaces ProductUnit: units now belong to the variant, because a 50 kg bag and a 25 kg bag convert differently.
 */
@Getter
public class VariantUnit {

    private final UUID variantUnitId;
    private final UUID unitId;
    private final boolean baseUnit;
    private int conversionFactor;
    private VariantUnitStatus unitStatus;
    private final Instant createdAt;
    private Instant updatedAt;

    private VariantUnit(UUID variantUnitId, UUID unitId, boolean baseUnit, int conversionFactor,
                        VariantUnitStatus unitStatus, Instant createdAt, Instant updatedAt) {
        this.variantUnitId = Objects.requireNonNull(variantUnitId);
        this.unitId = Objects.requireNonNull(unitId);
        this.baseUnit = baseUnit;
        this.conversionFactor = conversionFactor;
        this.unitStatus = Objects.requireNonNull(unitStatus);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    static VariantUnit createBase(UUID unitId) {
        Instant now = Instant.now();
        return new VariantUnit(UUID.randomUUID(), unitId, true, 1, VariantUnitStatus.ACTIVE, now, now);
    }

    static VariantUnit createNonBase(UUID unitId, int conversionFactor) {
        Instant now = Instant.now();
        return new VariantUnit(UUID.randomUUID(), unitId, false, conversionFactor, VariantUnitStatus.ACTIVE, now, now);
    }

    public static VariantUnit reconstitute(UUID variantUnitId, UUID unitId, boolean baseUnit, int conversionFactor,
                                           VariantUnitStatus unitStatus, Instant createdAt, Instant updatedAt) {
        return new VariantUnit(variantUnitId, unitId, baseUnit, conversionFactor, unitStatus, createdAt, updatedAt);
    }

    void changeConversionFactor(int factor) {
        this.conversionFactor = factor;
        touch();
    }

    void deactivate() {
        this.unitStatus = VariantUnitStatus.INACTIVE;
        touch();
    }

    public boolean isActive() { return unitStatus == VariantUnitStatus.ACTIVE; }

    private void touch() { this.updatedAt = Instant.now(); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof VariantUnit u && variantUnitId.equals(u.variantUnitId));
    }

    @Override
    public int hashCode() { return variantUnitId.hashCode(); }
}