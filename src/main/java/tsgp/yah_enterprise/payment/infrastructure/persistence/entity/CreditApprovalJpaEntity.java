package tsgp.yah_enterprise.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.payment.domain.model.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credit_approvals")
@Getter @Setter @NoArgsConstructor
public class CreditApprovalJpaEntity {

    @Id
    @Column(name = "credit_approval_id", nullable = false, updatable = false)
    private UUID creditApprovalId;

    @Column(name = "sale_id", nullable = false, updatable = false)
    private UUID saleId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "requested_amount", nullable = false, updatable = false)
    private long requestedAmount;

    @Column(name = "approved_amount")
    private Long approvedAmount;

    @Column(name = "reason", nullable = false, updatable = false, length = 500)
    private String reason;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private CreditApprovalStatus approvalStatus;

    @Column(name = "requested_by", nullable = false, updatable = false)
    private UUID requestedBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;
}
