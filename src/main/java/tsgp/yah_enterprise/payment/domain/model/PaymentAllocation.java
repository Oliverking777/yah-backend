package tsgp.yah_enterprise.payment.domain.model;

import tsgp.yah_enterprise.payment.domain.exception.InvalidPaymentAmountException;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Part of one payment applied to one sale. Never deleted: reversing a payment flips it to REVERSED. */
public class PaymentAllocation {

    private final UUID allocationId;
    private final UUID paymentId;
    private final UUID saleId;
    private final Money allocatedAmount;
    private AllocationStatus allocationStatus;
    private final Instant createdAt;
    private Instant reversedAt;

    private PaymentAllocation(UUID allocationId, UUID paymentId, UUID saleId, Money allocatedAmount,
                              AllocationStatus allocationStatus, Instant createdAt, Instant reversedAt) {
        this.allocationId = Objects.requireNonNull(allocationId);
        this.paymentId = Objects.requireNonNull(paymentId);
        this.saleId = Objects.requireNonNull(saleId);
        this.allocatedAmount = Objects.requireNonNull(allocatedAmount);
        if (!allocatedAmount.isPositive()) {
            throw new InvalidPaymentAmountException("The allocated amount must be greater than zero");
        }
        this.allocationStatus = Objects.requireNonNull(allocationStatus);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.reversedAt = reversedAt;
    }

    public static PaymentAllocation create(UUID paymentId, UUID saleId, Money amount) {
        return new PaymentAllocation(UUID.randomUUID(), paymentId, saleId, amount, AllocationStatus.ACTIVE,
                Instant.now(), null);
    }

    public static PaymentAllocation reconstitute(UUID allocationId, UUID paymentId, UUID saleId, Money amount,
                                                 AllocationStatus status, Instant createdAt, Instant reversedAt) {
        return new PaymentAllocation(allocationId, paymentId, saleId, amount, status, createdAt, reversedAt);
    }

    public void reverse() {
        if (allocationStatus == AllocationStatus.ACTIVE) {
            this.allocationStatus = AllocationStatus.REVERSED;
            this.reversedAt = Instant.now();
        }
    }

    public boolean isActive() {
        return allocationStatus == AllocationStatus.ACTIVE;
    }

    public UUID getAllocationId() { return allocationId; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getSaleId() { return saleId; }
    public Money getAllocatedAmount() { return allocatedAmount; }
    public AllocationStatus getAllocationStatus() { return allocationStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReversedAt() { return reversedAt; }
}
