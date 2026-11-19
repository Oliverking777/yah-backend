package tsgp.yah_enterprise.product.domain.model;

import lombok.Getter;
import tsgp.yah_enterprise.product.domain.exception.*;
import tsgp.yah_enterprise.product.domain.valueobject.VariantName;
import tsgp.yah_enterprise.product.domain.valueobject.VariantSku;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A separately stocked item of a product (the SKU), e.g. Indian Rice "50 kg 10% broken". Child of {@link Product}:
 * every change goes through the product, which keeps the rules that need all its variants.
 * Stock, prices and the lines of purchases, sales, transfers and adjustments refer to the variant id.
 * <p>
 * The "default" variant of a simple product has no name; it is hidden on screens and shows as the product.
 */
@Getter
public class ProductVariant {

    private final UUID variantId;
    private final VariantSku sku;
    private VariantName variantName;
    private VariantStatus variantStatus;
    private final List<VariantUnit> units;
    private final Instant createdAt;
    private Instant updatedAt;

    private ProductVariant(UUID variantId, VariantSku sku, VariantName variantName,
                           VariantStatus variantStatus, List<VariantUnit> units, Instant createdAt,
                           Instant updatedAt) {
        this.variantId = Objects.requireNonNull(variantId);
        this.sku = Objects.requireNonNull(sku);
        this.variantName = variantName;
        this.variantStatus = Objects.requireNonNull(variantStatus);
        this.units = new ArrayList<>(units);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    static ProductVariant create(UUID variantId, VariantSku sku, VariantName variantName, UUID baseUnitId) {
        Instant now = Instant.now();
        List<VariantUnit> units = new ArrayList<>();
        units.add(VariantUnit.createBase(Objects.requireNonNull(baseUnitId, "A base unit is required")));
        return new ProductVariant(variantId, sku, variantName, VariantStatus.ACTIVE, units, now, now);
    }

    public static ProductVariant reconstitute(UUID variantId, String sku, String variantName,
                                              VariantStatus variantStatus, List<VariantUnit> units,
                                              Instant createdAt, Instant updatedAt) {
        return new ProductVariant(variantId, VariantSku.of(sku), VariantName.ofNullable(variantName), variantStatus,
                units, createdAt, updatedAt);
    }

    public List<VariantUnit> getUnits() { return Collections.unmodifiableList(units); }

    /** True for the variant of a simple product: it has no name. */
    public boolean isDefault() { return variantName == null; }

    public boolean isActive() { return variantStatus == VariantStatus.ACTIVE; }

    public void ensureActive() {
        if (!isActive()) throw new VariantInactiveException(variantId);
    }

    /**
     * The name as compared for uniqueness within the product (lower case, no spaces); empty for the default variant.
     * Stored with a unique index per product, so the database refuses duplicates too.
     */
    public String nameKey() {
        return variantName == null ? "" : variantName.key();
    }

    // ---- changes, through the product ----

    void rename(VariantName newName) {
        this.variantName = newName;
        touch();
    }

    void activate() {
        if (isActive()) throw new VariantAlreadyActiveException(variantId);
        this.variantStatus = VariantStatus.ACTIVE;
        touch();
    }

    void deactivate() {
        if (!isActive()) throw new VariantAlreadyInactiveException(variantId);
        this.variantStatus = VariantStatus.INACTIVE;
        touch();
    }

    // ---- units ----

    void addUnit(UUID unitId, int conversionFactor) {
        if (units.stream().anyMatch(u -> u.getUnitId().equals(unitId))) {
            throw new ProductUnitAlreadyAssignedException(unitId);
        }
        checkNonBaseFactor(conversionFactor);
        units.add(VariantUnit.createNonBase(unitId, conversionFactor));
        touch();
    }

    void changeConversion(UUID unitId, int conversionFactor) {
        VariantUnit unit = findUnit(unitId);
        if (unit.isBaseUnit()) {
            throw new InvalidProductException("Cannot change the conversion factor of the base unit");
        }
        checkNonBaseFactor(conversionFactor);
        unit.changeConversionFactor(conversionFactor);
        touch();
    }

    void deactivateUnit(UUID unitId) {
        VariantUnit unit = findUnit(unitId);
        if (unit.isBaseUnit() && units.stream().anyMatch(u -> !u.isBaseUnit() && u.isActive())) {
            throw new BaseUnitProtectedException();
        }
        unit.deactivate();
        touch();
    }

    void removeUnit(UUID unitId) {
        VariantUnit unit = findUnit(unitId);
        if (unit.isBaseUnit()) throw new BaseUnitProtectedException();
        units.remove(unit);
        touch();
    }

    public VariantUnit findUnit(UUID unitId) {
        return units.stream()
                .filter(u -> u.getUnitId().equals(unitId))
                .findFirst()
                .orElseThrow(() -> new ProductUnitNotFoundException(unitId));
    }

    public Optional<VariantUnit> findBaseUnit() {
        return units.stream().filter(VariantUnit::isBaseUnit).findFirst();
    }

    /** Converts a quantity between two units of this variant; refused when it does not come out whole. */
    public long convert(UUID fromUnitId, UUID toUnitId, long quantity) {
        int fromFactor = findUnit(fromUnitId).getConversionFactor();
        int toFactor = findUnit(toUnitId).getConversionFactor();
        long baseQuantity = Math.multiplyExact(quantity, fromFactor);
        if (baseQuantity % toFactor != 0) {
            throw new InvalidConversionException(
                    "Cannot convert " + quantity + " units exactly from " + fromUnitId + " to " + toUnitId);
        }
        return baseQuantity / toFactor;
    }

    // ---- helpers ----

    private static void checkNonBaseFactor(int conversionFactor) {
        if (conversionFactor < 2) {
            throw new InvalidProductException("Non-base conversion factor must be >= 2");
        }
    }

    private void touch() { this.updatedAt = Instant.now(); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ProductVariant v && variantId.equals(v.variantId));
    }

    @Override
    public int hashCode() { return variantId.hashCode(); }
}