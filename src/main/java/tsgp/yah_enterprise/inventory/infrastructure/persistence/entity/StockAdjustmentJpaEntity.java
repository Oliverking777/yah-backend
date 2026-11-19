package tsgp.yah_enterprise.inventory.infrastructure.persistence.entity;

import tsgp.yah_enterprise.inventory.domain.model.AdjustmentDirection;
import tsgp.yah_enterprise.inventory.domain.model.AdjustmentReason;
import tsgp.yah_enterprise.inventory.domain.model.AdjustmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_adjustments")
@Getter @Setter @NoArgsConstructor
public class StockAdjustmentJpaEntity {

    @Id
    @Column(name = "adjustment_id", nullable = false, updatable = false)
    private UUID adjustmentId;

    @Column(name = "adjustment_number", nullable = false, updatable = false, length = 20)
    private String adjustmentNumber;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, updatable = false, length = 10)
    private AdjustmentDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_category", nullable = false, updatable = false, length = 30)
    private AdjustmentReason reasonCategory;

    @Column(name = "quantity", nullable = false, updatable = false)
    private long quantity;

    @Column(name = "note", updatable = false, length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_status", nullable = false, length = 20)
    private AdjustmentStatus adjustmentStatus;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "decided_by")
    private UUID decidedBy;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    @Column(name = "before_quantity")
    private Long beforeQuantity;

    @Column(name = "after_quantity")
    private Long afterQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
