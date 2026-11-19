package tsgp.yah_enterprise.payment.domain.model;

import tsgp.yah_enterprise.payment.domain.exception.InvalidPaymentAmountException;
import tsgp.yah_enterprise.payment.domain.exception.InvalidPaymentException;
import tsgp.yah_enterprise.payment.domain.exception.ReceivableOverAllocationException;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * What one sale owes; its key is the saleId. Debt is derived, never edited (F9): outstanding = total - allocated.
 * Created only by SettleSale, even when the sale is fully paid on the spot.
 */
public class Receivable {

    private final UUID saleId;
    private final UUID customerId;
    private final UUID locationId;
    private final Money totalAmount;
    private Money allocatedAmount;
    private final Long version;
    private final Instant createdAt;
    private Instant updatedAt;

    private Receivable(UUID saleId, UUID customerId, UUID locationId, Money totalAmount, Money allocatedAmount,
                       Long version, Instant createdAt, Instant updatedAt) {
        this.saleId = Objects.requireNonNull(saleId, "saleId");
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.locationId = Objects.requireNonNull(locationId, "locationId");
        this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount");
        if (!totalAmount.isPositive()) {
            throw new InvalidPaymentException("The total of a sale must be greater than zero");
        }
        this.allocatedAmount = Objects.requireNonNull(allocatedAmount, "allocatedAmount");
        if (allocatedAmount.isGreaterThan(totalAmount)) {
            throw new InvalidPaymentException("Allocated amount cannot be more than the total");
        }
        this.version = version;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static Receivable open(UUID saleId, UUID customerId, UUID locationId, Money totalAmount) {
        Instant now = Instant.now();
        return new Receivable(saleId, customerId, locationId, totalAmount, Money.ZERO, null, now, now);
    }

    public static Receivable reconstitute(UUID saleId, UUID customerId, UUID locationId, Money totalAmount,
                                          Money allocatedAmount, Long version, Instant createdAt, Instant updatedAt) {
        return new Receivable(saleId, customerId, locationId, totalAmount, allocatedAmount, version, createdAt,
                updatedAt);
    }

    public void allocate(Money part) {
        if (!part.isPositive()) {
            throw new InvalidPaymentAmountException("The allocated amount must be greater than zero");
        }
        if (part.isGreaterThan(outstanding())) {
            throw new ReceivableOverAllocationException("Sale " + saleId + " owes only " + outstanding()
                    + " FCFA, not " + part);
        }
        this.allocatedAmount = allocatedAmount.plus(part);
        touch();
    }

    /** Gives allocated money back: the debt returns. */
    public void release(Money part) {
        if (part.isGreaterThan(allocatedAmount)) {
            throw new InvalidPaymentException("Cannot release more than is allocated");
        }
        this.allocatedAmount = allocatedAmount.minus(part);
        touch();
    }

    public Money outstanding() {
        return totalAmount.minus(allocatedAmount);
    }

    public ReceivableStatus receivableStatus() {
        if (allocatedAmount.isZero()) {
            return ReceivableStatus.OPEN;
        }
        return outstanding().isZero() ? ReceivableStatus.PAID : ReceivableStatus.PARTIALLY_PAID;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getSaleId() { return saleId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getLocationId() { return locationId; }
    public Money getTotalAmount() { return totalAmount; }
    public Money getAllocatedAmount() { return allocatedAmount; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
