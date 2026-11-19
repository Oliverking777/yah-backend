package tsgp.yah_enterprise.payment.domain.model;

import tsgp.yah_enterprise.payment.domain.exception.*;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Permission to sell on credit: requested by an employee, decided by a Manager or Owner who is never the requester.
 * Only a REQUESTED approval can be decided (once). Only an APPROVED one can be consumed (once).
 */
public class CreditApproval {

    public static final int MIN_TEXT_LENGTH = 5;
    public static final int MAX_TEXT_LENGTH = 500;

    private final UUID creditApprovalId;
    private final UUID saleId;
    private final UUID customerId;
    private final Money requestedAmount;
    private Money approvedAmount;
    private final String reason;
    private String decisionNote;
    private CreditApprovalStatus approvalStatus;
    private final UUID requestedBy;
    private UUID approvedBy;
    private final Instant requestedAt;
    private Instant decidedAt;
    private Instant consumedAt;

    private CreditApproval(UUID creditApprovalId, UUID saleId, UUID customerId, Money requestedAmount,
                           Money approvedAmount, String reason, String decisionNote,
                           CreditApprovalStatus approvalStatus, UUID requestedBy, UUID approvedBy,
                           Instant requestedAt, Instant decidedAt, Instant consumedAt) {
        this.creditApprovalId = Objects.requireNonNull(creditApprovalId);
        this.saleId = Objects.requireNonNull(saleId);
        this.customerId = Objects.requireNonNull(customerId);
        this.requestedAmount = Objects.requireNonNull(requestedAmount);
        this.approvedAmount = approvedAmount;
        this.reason = Objects.requireNonNull(reason);
        this.decisionNote = decisionNote;
        this.approvalStatus = Objects.requireNonNull(approvalStatus);
        this.requestedBy = Objects.requireNonNull(requestedBy);
        this.approvedBy = approvedBy;
        this.requestedAt = Objects.requireNonNull(requestedAt);
        this.decidedAt = decidedAt;
        this.consumedAt = consumedAt;
    }

    public static CreditApproval request(UUID saleId, UUID customerId, Money requestedAmount, String reason,
                                         UUID requestedBy) {
        if (saleId == null || customerId == null || requestedBy == null || requestedAmount == null) {
            throw new InvalidCreditRequestException("Sale, customer, amount and the requesting user are required");
        }
        if (!requestedAmount.isPositive()) {
            throw new InvalidCreditRequestException("The requested amount must be greater than zero");
        }
        return new CreditApproval(UUID.randomUUID(), saleId, customerId, requestedAmount, null,
                requireText(reason, "Reason"), null, CreditApprovalStatus.REQUESTED, requestedBy, null,
                Instant.now(), null, null);
    }

    public static CreditApproval reconstitute(UUID creditApprovalId, UUID saleId, UUID customerId,
                                              Money requestedAmount, Money approvedAmount, String reason,
                                              String decisionNote, CreditApprovalStatus approvalStatus,
                                              UUID requestedBy, UUID approvedBy, Instant requestedAt,
                                              Instant decidedAt, Instant consumedAt) {
        return new CreditApproval(creditApprovalId, saleId, customerId, requestedAmount, approvedAmount, reason,
                decisionNote, approvalStatus, requestedBy, approvedBy, requestedAt, decidedAt, consumedAt);
    }

    /** approvedAmount null means the requested amount. It must be between 1 and the requested amount. */
    public void approve(UUID userId, Money approved, String note) {
        requireDecidable(userId);
        Money amount = approved == null ? requestedAmount : approved;
        if (!amount.isPositive() || amount.isGreaterThan(requestedAmount)) {
            throw new InvalidCreditRequestException("The approved amount must be between 1 and " + requestedAmount);
        }
        String cleaned = note == null || note.isBlank() ? null : note.trim();
        if (cleaned != null && cleaned.length() > MAX_TEXT_LENGTH) {
            throw new InvalidCreditRequestException("Note must be at most " + MAX_TEXT_LENGTH + " characters");
        }
        this.approvedAmount = amount;
        this.decisionNote = cleaned;
        this.approvalStatus = CreditApprovalStatus.APPROVED;
        markDecided(userId);
    }

    public void reject(UUID userId, String note) {
        requireDecidable(userId);
        this.decisionNote = requireText(note, "Note");
        this.approvalStatus = CreditApprovalStatus.REJECTED;
        markDecided(userId);
    }

    /** Used once by a completed sale. */
    public void consume() {
        if (approvalStatus != CreditApprovalStatus.APPROVED) {
            throw new CreditApprovalMismatchException("Credit approval " + creditApprovalId
                    + " cannot be used: it is " + approvalStatus);
        }
        this.approvalStatus = CreditApprovalStatus.CONSUMED;
        this.consumedAt = Instant.now();
    }

    public boolean isApproved() {
        return approvalStatus == CreditApprovalStatus.APPROVED;
    }

    /** REQUESTED or APPROVED: at most one such approval exists per sale. */
    public boolean isActive() {
        return approvalStatus == CreditApprovalStatus.REQUESTED || approvalStatus == CreditApprovalStatus.APPROVED;
    }

    private void requireDecidable(UUID userId) {
        if (approvalStatus != CreditApprovalStatus.REQUESTED) {
            throw new CreditApprovalAlreadyDecidedException("Credit approval " + creditApprovalId
                    + " was already decided: " + approvalStatus);
        }
        Objects.requireNonNull(userId, "userId");
        if (userId.equals(requestedBy)) {
            throw new SelfCreditApprovalNotAllowedException("You cannot decide a credit request you made yourself");
        }
    }

    private void markDecided(UUID userId) {
        this.approvedBy = userId;
        this.decidedAt = Instant.now();
    }

    private static String requireText(String value, String label) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() < MIN_TEXT_LENGTH || cleaned.length() > MAX_TEXT_LENGTH) {
            throw new InvalidCreditRequestException(label + " must be " + MIN_TEXT_LENGTH + " to "
                    + MAX_TEXT_LENGTH + " characters");
        }
        return cleaned;
    }

    public UUID getCreditApprovalId() { return creditApprovalId; }
    public UUID getSaleId() { return saleId; }
    public UUID getCustomerId() { return customerId; }
    public Money getRequestedAmount() { return requestedAmount; }
    public Money getApprovedAmount() { return approvedAmount; }
    public String getReason() { return reason; }
    public String getDecisionNote() { return decisionNote; }
    public CreditApprovalStatus getApprovalStatus() { return approvalStatus; }
    public UUID getRequestedBy() { return requestedBy; }
    public UUID getApprovedBy() { return approvedBy; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public Instant getConsumedAt() { return consumedAt; }
}
