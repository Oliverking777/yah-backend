package tsgp.yah_enterprise.payment.application.service;

import tsgp.yah_enterprise.payment.application.dto.*;
import tsgp.yah_enterprise.payment.domain.exception.*;
import tsgp.yah_enterprise.payment.domain.model.*;
import tsgp.yah_enterprise.payment.domain.port.*;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;

/** Shared checks and mapping of the Payment services. */
final class PaymentSupport {

    private final PaymentLocationDirectoryPort locationDirectory;

    PaymentSupport(PaymentLocationDirectoryPort locationDirectory) {
        this.locationDirectory = locationDirectory;
    }

    void requireAtLocation(PaymentActor actor, java.util.UUID locationId) {
        requireActor(actor);
        if (!actor.anyLocation() && !locationDirectory.isUserAssigned(actor.userId(), locationId)) {
            throw new PaymentLocationAccessException("You are not assigned to this location");
        }
    }

    void requireUsableLocation(java.util.UUID locationId) {
        if (!locationDirectory.isActive(locationId)) {
            throw new PaymentLocationNotUsableException("Location " + locationId + " does not exist or is inactive");
        }
    }

    static void requireActor(PaymentActor actor) {
        if (actor == null || actor.userId() == null) {
            throw new InvalidPaymentException("The acting user is required");
        }
    }

    /** ALL users may filter by any location (or none); the others always see only their own location. */
    java.util.UUID resolveScope(PaymentActor actor, java.util.UUID requested) {
        requireActor(actor);
        if (actor.anyLocation()) {
            return requested;
        }
        java.util.UUID own = locationDirectory.assignedLocationOf(actor.userId())
                .orElseThrow(() -> new PaymentLocationAccessException("You are not assigned to a location"));
        if (requested != null && !requested.equals(own)) {
            throw new PaymentLocationAccessException("You are not assigned to this location");
        }
        return own;
    }

    java.util.Optional<java.util.UUID> assignedLocation(java.util.UUID userId) {
        return locationDirectory.assignedLocationOf(userId);
    }

    static PaymentResult toResult(Payment p) {
        return new PaymentResult(p.getPaymentId(), p.getPaymentNumber().value(), p.getCustomerId(),
                p.getLocationId(), p.getAmount().amount(), p.getPaymentMethod().name(), p.getReference(),
                p.getReceivedBy(), p.getReceivedAt(), p.getPaymentStatus().name(), p.getAllocatedAmount().amount(),
                p.unallocated().amount(), p.getReversedBy(), p.getReversalReason(), p.getReversedAt(),
                p.getCreatedAt(), p.getUpdatedAt());
    }

    static AllocationResult toResult(PaymentAllocation a) {
        return new AllocationResult(a.getAllocationId(), a.getPaymentId(), a.getSaleId(),
                a.getAllocatedAmount().amount(), a.getAllocationStatus().name(), a.getCreatedAt(),
                a.getReversedAt());
    }

    static DebtResult toResult(Receivable r) {
        return new DebtResult(r.getSaleId(), r.getCustomerId(), r.getLocationId(), r.getTotalAmount().amount(),
                r.getAllocatedAmount().amount(), r.outstanding().amount(), r.receivableStatus().name(),
                r.getCreatedAt());
    }

    static CreditApprovalResult toResult(CreditApproval c) {
        return new CreditApprovalResult(c.getCreditApprovalId(), c.getSaleId(), c.getCustomerId(),
                c.getRequestedAmount().amount(),
                c.getApprovedAmount() == null ? null : c.getApprovedAmount().amount(), c.getReason(),
                c.getDecisionNote(), c.getApprovalStatus().name(), c.getRequestedBy(), c.getApprovedBy(),
                c.getRequestedAt(), c.getDecidedAt(), c.getConsumedAt());
    }
}
