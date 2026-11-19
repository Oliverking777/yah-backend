package tsgp.yah_enterprise.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.payment.domain.model.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "receivables")
@Getter @Setter @NoArgsConstructor
public class ReceivableJpaEntity {

    @Id
    @Column(name = "sale_id", nullable = false, updatable = false)
    private UUID saleId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Column(name = "total_amount", nullable = false, updatable = false)
    private long totalAmount;

    @Column(name = "allocated_amount", nullable = false)
    private long allocatedAmount;

    /** Two people allocating to the same sale at once: the second one fails instead of overwriting. */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
