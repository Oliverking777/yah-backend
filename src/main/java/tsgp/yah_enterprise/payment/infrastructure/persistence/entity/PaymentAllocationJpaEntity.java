package tsgp.yah_enterprise.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.payment.domain.model.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_allocations")
@Getter @Setter @NoArgsConstructor
public class PaymentAllocationJpaEntity {

    @Id
    @Column(name = "allocation_id", nullable = false, updatable = false)
    private UUID allocationId;

    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "sale_id", nullable = false, updatable = false)
    private UUID saleId;

    @Column(name = "allocated_amount", nullable = false, updatable = false)
    private long allocatedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_status", nullable = false, length = 20)
    private AllocationStatus allocationStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "reversed_at")
    private Instant reversedAt;
}
