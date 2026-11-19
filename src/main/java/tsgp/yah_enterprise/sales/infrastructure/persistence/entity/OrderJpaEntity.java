package tsgp.yah_enterprise.sales.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import tsgp.yah_enterprise.sales.domain.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class OrderJpaEntity {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "order_number", nullable = false, updatable = false, length = 20)
    private String orderNumber;

    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(name = "subtotal", nullable = false)
    private long subtotal;

    @Column(name = "discount_total", nullable = false)
    private long discountTotal;

    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @Column(name = "amount_paid")
    private Long amountPaid;

    @Column(name = "amount_due")
    private Long amountDue;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "validated_by")
    private UUID validatedBy;

    @Column(name = "completed_by")
    private UUID completedBy;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @BatchSize(size = 50) // avoids one extra query per order when listing
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
