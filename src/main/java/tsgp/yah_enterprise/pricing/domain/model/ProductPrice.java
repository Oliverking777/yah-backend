package tsgp.yah_enterprise.pricing.domain.model;

import lombok.Getter;
import tsgp.yah_enterprise.pricing.domain.exception.InvalidPriceException;
import tsgp.yah_enterprise.pricing.domain.exception.PriceAlreadyInactiveException;
import tsgp.yah_enterprise.pricing.domain.valueobject.Money;
import tsgp.yah_enterprise.pricing.domain.valueobject.QuantityRange;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * The price of one unit of a product for a quantity range and a period.
 * Owned by the Pricing module. Amount, range and start never change: a price
 * change closes this row and creates a successor (see {@link #supersede}).
 */
@Getter
public class ProductPrice {

    private final UUID priceId;
    private final UUID productId;
    private final UUID unitId;
    private final Money price;
    private final QuantityRange quantityRange;
    private final Instant effectiveFrom;
    private Instant effectiveTo;           // exclusive end, null = open ended
    private PriceStatus priceStatus;
    private final Instant createdAt;
    private Instant updatedAt;

    private ProductPrice(UUID priceId, UUID productId, UUID unitId, Money price,
                         QuantityRange quantityRange, Instant effectiveFrom, Instant effectiveTo,
                         PriceStatus priceStatus, Instant createdAt, Instant updatedAt) {
        this.priceId = Objects.requireNonNull(priceId, "priceId must not be null");
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.price = Objects.requireNonNull(price, "price must not be null");
        this.quantityRange = Objects.requireNonNull(quantityRange, "quantityRange must not be null");
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom must not be null");
        if (effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
            throw new InvalidPriceException("The end of the price period must be after its start");
        }
        this.effectiveTo = effectiveTo;
        this.priceStatus = Objects.requireNonNull(priceStatus, "priceStatus must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /** Creates a brand new, active price. A null effectiveFrom means "now". */
    public static ProductPrice create(UUID productId, UUID unitId, long price,
                                      int minimumQuantity, Integer maximumQuantity,
                                      Instant effectiveFrom, Instant effectiveTo) {
        Instant now = Instant.now();
        return new ProductPrice(UUID.randomUUID(), productId, unitId, Money.of(price),
                QuantityRange.of(minimumQuantity, maximumQuantity),
                effectiveFrom == null ? now : effectiveFrom, effectiveTo,
                PriceStatus.ACTIVE, now, now);
    }

    /** Rebuilds an existing price from stored data; fires no rule and bumps no timestamp. */
    public static ProductPrice reconstitute(UUID priceId, UUID productId, UUID unitId, long price,
                                            int minimumQuantity, Integer maximumQuantity,
                                            Instant effectiveFrom, Instant effectiveTo,
                                            PriceStatus priceStatus, Instant createdAt, Instant updatedAt) {
        return new ProductPrice(priceId, productId, unitId, Money.of(price),
                QuantityRange.of(minimumQuantity, maximumQuantity),
                effectiveFrom, effectiveTo, priceStatus, createdAt, updatedAt);
    }

    /**
     * Changes the amount and/or range without rewriting history: this row is closed
     * at {@code at} and the returned successor carries the new values. The caller must
     * run the overlap check on the successor, then save this row first and the successor second.
     */
    public ProductPrice supersede(Money newAmount, QuantityRange newRange, Instant at) {
        if (priceStatus == PriceStatus.INACTIVE) {
            throw new PriceAlreadyInactiveException(priceId);
        }
        Instant start = at.isAfter(effectiveFrom) ? at : effectiveFrom; // a not-yet-started price keeps its schedule
        if (effectiveTo != null && !effectiveTo.isAfter(start)) {
            throw new InvalidPriceException("An expired price cannot be changed; create a new price instead");
        }
        ProductPrice successor = new ProductPrice(UUID.randomUUID(), productId, unitId,
                newAmount, newRange, start, effectiveTo, PriceStatus.ACTIVE, at, at);
        closeAt(at);
        return successor;
    }

    public void deactivate() {
        if (priceStatus == PriceStatus.INACTIVE) {
            throw new PriceAlreadyInactiveException(priceId);
        }
        this.priceStatus = PriceStatus.INACTIVE;
        touch();
    }

    public boolean isActive() {
        return priceStatus == PriceStatus.ACTIVE;
    }

    /** True when the moment falls inside the period; the end is exclusive. */
    public boolean isEffectiveAt(Instant at) {
        return !at.isBefore(effectiveFrom) && (effectiveTo == null || at.isBefore(effectiveTo));
    }

    /** Two active prices of the same product and unit conflict when both range and period overlap. */
    public boolean conflictsWith(ProductPrice other) {
        if (priceId.equals(other.priceId) || !isActive() || !other.isActive()
                || !productId.equals(other.productId) || !unitId.equals(other.unitId)) {
            return false;
        }
        return quantityRange.overlaps(other.quantityRange) && periodOverlaps(other);
    }

    private boolean periodOverlaps(ProductPrice other) {
        boolean startsBeforeOtherEnds = other.effectiveTo == null || effectiveFrom.isBefore(other.effectiveTo);
        boolean otherStartsBeforeEnd = effectiveTo == null || other.effectiveFrom.isBefore(effectiveTo);
        return startsBeforeOtherEnds && otherStartsBeforeEnd;
    }

    private void closeAt(Instant at) {
        if (at.isAfter(effectiveFrom) && (effectiveTo == null || at.isBefore(effectiveTo))) {
            this.effectiveTo = at;
        }
        this.priceStatus = PriceStatus.INACTIVE;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ProductPrice other && priceId.equals(other.priceId);
    }

    @Override
    public int hashCode() {
        return priceId.hashCode();
    }
}
