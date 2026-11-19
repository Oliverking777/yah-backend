package tsgp.yah_enterprise.payment.domain.model;

import tsgp.yah_enterprise.payment.domain.exception.*;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;
import tsgp.yah_enterprise.payment.domain.valueobject.PaymentNumber;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Money received from a customer. A sale and a payment are different events: one payment can pay several sales,
 * and one sale can have several partial payments. A payment is never deleted: a wrong one is reversed.
 */
public class Payment {

    public static final int MIN_REFERENCE_LENGTH = 3;
    public static final int MAX_REFERENCE_LENGTH = 50;
    public static final int MIN_REASON_LENGTH = 5;
    public static final int MAX_REASON_LENGTH = 500;

    private final UUID paymentId;
    private final PaymentNumber paymentNumber;
    private final UUID customerId;
    private final UUID locationId;
    private final Money amount;
    private final PaymentMethod paymentMethod;
    private final String reference;
    private final UUID receivedBy;
    private final Instant receivedAt;
    private PaymentStatus paymentStatus;
    private Money allocatedAmount;
    private UUID reversedBy;
    private String reversalReason;
    private Instant reversedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private Payment(UUID paymentId, PaymentNumber paymentNumber, UUID customerId, UUID locationId, Money amount,
                    PaymentMethod paymentMethod, String reference, UUID receivedBy, Instant receivedAt,
                    PaymentStatus paymentStatus, Money allocatedAmount, UUID reversedBy, String reversalReason,
                    Instant reversedAt, Instant createdAt, Instant updatedAt) {
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId");
        this.paymentNumber = Objects.requireNonNull(paymentNumber, "paymentNumber");
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.locationId = Objects.requireNonNull(locationId, "locationId");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "paymentMethod");
        this.reference = reference;
        this.receivedBy = Objects.requireNonNull(receivedBy, "receivedBy");
        this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt");
        this.paymentStatus = Objects.requireNonNull(paymentStatus, "paymentStatus");
        this.allocatedAmount = Objects.requireNonNull(allocatedAmount, "allocatedAmount");
        this.reversedBy = reversedBy;
        this.reversalReason = reversalReason;
        this.reversedAt = reversedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    /**
     * Records a payment. paymentId is the client's UUID for offline work or null. receivedAt may be earlier than now
     * (offline work) but never in the future. MOBILE_MONEY needs the provider reference; CASH has none.
     * Never store card, PIN or wallet credentials here.
     */
    public static Payment record(UUID paymentId, PaymentNumber number, UUID customerId, UUID locationId, Money amount,
                                 PaymentMethod method, String reference, UUID receivedBy, Instant receivedAt) {
        if (customerId == null || locationId == null || receivedBy == null || method == null || amount == null) {
            throw new InvalidPaymentException("Customer, location, amount, method and the receiving user are required");
        }
        if (!amount.isPositive()) {
            throw new InvalidPaymentAmountException("The payment amount must be greater than zero");
        }
        Instant now = Instant.now();
        Instant when = receivedAt == null ? now : receivedAt;
        if (when.isAfter(now)) {
            throw new InvalidPaymentException("The payment cannot be received in the future");
        }
        return new Payment(paymentId == null ? UUID.randomUUID() : paymentId, number, customerId, locationId, amount,
                method, cleanReference(method, reference), receivedBy, when, PaymentStatus.RECORDED, Money.ZERO,
                null, null, null, now, now);
    }

    public static Payment reconstitute(UUID paymentId, PaymentNumber paymentNumber, UUID customerId, UUID locationId,
                                       Money amount, PaymentMethod paymentMethod, String reference, UUID receivedBy,
                                       Instant receivedAt, PaymentStatus paymentStatus, Money allocatedAmount,
                                       UUID reversedBy, String reversalReason, Instant reversedAt, Instant createdAt,
                                       Instant updatedAt) {
        return new Payment(paymentId, paymentNumber, customerId, locationId, amount, paymentMethod, reference,
                receivedBy, receivedAt, paymentStatus, allocatedAmount, reversedBy, reversalReason, reversedAt,
                createdAt, updatedAt);
    }

    /** Sets aside part of this payment for a sale. */
    public void allocate(Money part) {
        requireRecorded();
        if (!part.isPositive()) {
            throw new InvalidPaymentAmountException("The allocated amount must be greater than zero");
        }
        if (part.isGreaterThan(unallocated())) {
            throw new PaymentOverAllocationException("Payment " + paymentNumber + " has only " + unallocated()
                    + " FCFA left to allocate, not " + part);
        }
        this.allocatedAmount = allocatedAmount.plus(part);
        touch();
    }

    /** Takes back part of an allocation. */
    public void release(Money part) {
        if (part.isGreaterThan(allocatedAmount)) {
            throw new InvalidPaymentException("Cannot release more than is allocated");
        }
        this.allocatedAmount = allocatedAmount.minus(part);
        touch();
    }

    /** A Manager or Owner reverses a RECORDED payment with a reason. The caller reverses its allocations too. */
    public void reverse(UUID userId, String reason) {
        if (paymentStatus == PaymentStatus.REVERSED) {
            throw new PaymentAlreadyReversedException("Payment " + paymentNumber + " is already reversed");
        }
        String cleaned = reason == null ? "" : reason.trim();
        if (cleaned.length() < MIN_REASON_LENGTH || cleaned.length() > MAX_REASON_LENGTH) {
            throw new InvalidPaymentException("A reversal reason of " + MIN_REASON_LENGTH + " to "
                    + MAX_REASON_LENGTH + " characters is required");
        }
        this.paymentStatus = PaymentStatus.REVERSED;
        this.reversedBy = Objects.requireNonNull(userId, "userId");
        this.reversalReason = cleaned;
        this.reversedAt = Instant.now();
        this.allocatedAmount = Money.ZERO;
        touch();
    }

    public Money unallocated() {
        return amount.minus(allocatedAmount);
    }

    public boolean isRecorded() {
        return paymentStatus == PaymentStatus.RECORDED;
    }

    private void requireRecorded() {
        if (paymentStatus != PaymentStatus.RECORDED) {
            throw new PaymentAlreadyReversedException("Payment " + paymentNumber + " is reversed");
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String cleanReference(PaymentMethod method, String reference) {
        String cleaned = reference == null ? "" : reference.trim();
        if (method == PaymentMethod.MOBILE_MONEY) {
            if (cleaned.length() < MIN_REFERENCE_LENGTH || cleaned.length() > MAX_REFERENCE_LENGTH) {
                throw new InvalidPaymentException("A mobile money payment needs the provider reference ("
                        + MIN_REFERENCE_LENGTH + " to " + MAX_REFERENCE_LENGTH + " characters)");
            }
            return cleaned;
        }
        if (!cleaned.isEmpty()) {
            throw new InvalidPaymentException("A cash payment has no reference");
        }
        return null;
    }

    public UUID getPaymentId() { return paymentId; }
    public PaymentNumber getPaymentNumber() { return paymentNumber; }
    public UUID getCustomerId() { return customerId; }
    public UUID getLocationId() { return locationId; }
    public Money getAmount() { return amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getReference() { return reference; }
    public UUID getReceivedBy() { return receivedBy; }
    public Instant getReceivedAt() { return receivedAt; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public Money getAllocatedAmount() { return allocatedAmount; }
    public UUID getReversedBy() { return reversedBy; }
    public String getReversalReason() { return reversalReason; }
    public Instant getReversedAt() { return reversedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
