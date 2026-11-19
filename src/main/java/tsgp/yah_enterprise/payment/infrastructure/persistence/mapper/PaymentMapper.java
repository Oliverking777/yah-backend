package tsgp.yah_enterprise.payment.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.payment.domain.model.*;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;
import tsgp.yah_enterprise.payment.domain.valueobject.PaymentNumber;
import tsgp.yah_enterprise.payment.infrastructure.persistence.entity.*;

@Component
public class PaymentMapper {

    public PaymentJpaEntity toEntity(Payment p) {
        PaymentJpaEntity e = new PaymentJpaEntity();
        e.setPaymentId(p.getPaymentId());
        e.setPaymentNumber(p.getPaymentNumber().value());
        e.setCustomerId(p.getCustomerId());
        e.setLocationId(p.getLocationId());
        e.setAmount(p.getAmount().amount());
        e.setPaymentMethod(p.getPaymentMethod());
        e.setReference(p.getReference());
        e.setReceivedBy(p.getReceivedBy());
        e.setReceivedAt(p.getReceivedAt());
        e.setPaymentStatus(p.getPaymentStatus());
        e.setAllocatedAmount(p.getAllocatedAmount().amount());
        e.setReversedBy(p.getReversedBy());
        e.setReversalReason(p.getReversalReason());
        e.setReversedAt(p.getReversedAt());
        e.setCreatedAt(p.getCreatedAt());
        e.setUpdatedAt(p.getUpdatedAt());
        return e;
    }

    public Payment toDomain(PaymentJpaEntity e) {
        return Payment.reconstitute(e.getPaymentId(), PaymentNumber.of(e.getPaymentNumber()), e.getCustomerId(),
                e.getLocationId(), Money.of(e.getAmount()), e.getPaymentMethod(), e.getReference(),
                e.getReceivedBy(), e.getReceivedAt(), e.getPaymentStatus(), Money.of(e.getAllocatedAmount()),
                e.getReversedBy(), e.getReversalReason(), e.getReversedAt(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public ReceivableJpaEntity toEntity(Receivable r) {
        ReceivableJpaEntity e = new ReceivableJpaEntity();
        e.setSaleId(r.getSaleId());
        e.setCustomerId(r.getCustomerId());
        e.setLocationId(r.getLocationId());
        e.setTotalAmount(r.getTotalAmount().amount());
        e.setAllocatedAmount(r.getAllocatedAmount().amount());
        e.setVersion(r.getVersion());
        e.setCreatedAt(r.getCreatedAt());
        e.setUpdatedAt(r.getUpdatedAt());
        return e;
    }

    public Receivable toDomain(ReceivableJpaEntity e) {
        return Receivable.reconstitute(e.getSaleId(), e.getCustomerId(), e.getLocationId(),
                Money.of(e.getTotalAmount()), Money.of(e.getAllocatedAmount()), e.getVersion(), e.getCreatedAt(),
                e.getUpdatedAt());
    }

    public PaymentAllocationJpaEntity toEntity(PaymentAllocation a) {
        PaymentAllocationJpaEntity e = new PaymentAllocationJpaEntity();
        e.setAllocationId(a.getAllocationId());
        e.setPaymentId(a.getPaymentId());
        e.setSaleId(a.getSaleId());
        e.setAllocatedAmount(a.getAllocatedAmount().amount());
        e.setAllocationStatus(a.getAllocationStatus());
        e.setCreatedAt(a.getCreatedAt());
        e.setReversedAt(a.getReversedAt());
        return e;
    }

    public PaymentAllocation toDomain(PaymentAllocationJpaEntity e) {
        return PaymentAllocation.reconstitute(e.getAllocationId(), e.getPaymentId(), e.getSaleId(),
                Money.of(e.getAllocatedAmount()), e.getAllocationStatus(), e.getCreatedAt(), e.getReversedAt());
    }

    public CreditApprovalJpaEntity toEntity(CreditApproval c) {
        CreditApprovalJpaEntity e = new CreditApprovalJpaEntity();
        e.setCreditApprovalId(c.getCreditApprovalId());
        e.setSaleId(c.getSaleId());
        e.setCustomerId(c.getCustomerId());
        e.setRequestedAmount(c.getRequestedAmount().amount());
        e.setApprovedAmount(c.getApprovedAmount() == null ? null : c.getApprovedAmount().amount());
        e.setReason(c.getReason());
        e.setDecisionNote(c.getDecisionNote());
        e.setApprovalStatus(c.getApprovalStatus());
        e.setRequestedBy(c.getRequestedBy());
        e.setApprovedBy(c.getApprovedBy());
        e.setRequestedAt(c.getRequestedAt());
        e.setDecidedAt(c.getDecidedAt());
        e.setConsumedAt(c.getConsumedAt());
        return e;
    }

    public CreditApproval toDomain(CreditApprovalJpaEntity e) {
        return CreditApproval.reconstitute(e.getCreditApprovalId(), e.getSaleId(), e.getCustomerId(),
                Money.of(e.getRequestedAmount()),
                e.getApprovedAmount() == null ? null : Money.of(e.getApprovedAmount()), e.getReason(),
                e.getDecisionNote(), e.getApprovalStatus(), e.getRequestedBy(), e.getApprovedBy(),
                e.getRequestedAt(), e.getDecidedAt(), e.getConsumedAt());
    }
}
