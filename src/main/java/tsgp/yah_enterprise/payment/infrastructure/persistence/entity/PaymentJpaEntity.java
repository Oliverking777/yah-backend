package tsgp.yah_enterprise.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.payment.domain.model.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor
public class PaymentJpaEntity {

    @Id
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "payment_number", nullable = false, updatable = false, length = 20)
    private String paymentNumber;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Column(name = "amount", nullable = false, updatable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, updatable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "reference", updatable = false, length = 50)
    private String reference;

    @Column(name = "received_by", nullable = false, updatable = false)
    private UUID receivedBy;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "allocated_amount", nullable = false)
    private long allocatedAmount;

    @Column(name = "reversed_by")
    private UUID reversedBy;

    @Column(name = "reversal_reason", length = 500)
    private String reversalReason;

    @Column(name = "reversed_at")
    private Instant reversedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
