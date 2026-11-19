package tsgp.yah_enterprise.inventory.infrastructure.persistence.entity;

import tsgp.yah_enterprise.inventory.domain.model.MovementType;
import tsgp.yah_enterprise.inventory.domain.model.ReferenceType;
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

/** Insert-only: the database refuses UPDATE and DELETE on this table (see V19). */
@Entity
@Table(name = "stock_movements")
@Getter @Setter @NoArgsConstructor
public class StockMovementJpaEntity {

    @Id
    @Column(name = "movement_id", nullable = false, updatable = false)
    private UUID movementId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, updatable = false, length = 20)
    private MovementType movementType;

    @Column(name = "quantity_delta", nullable = false, updatable = false)
    private long quantityDelta;

    @Column(name = "balance_after", nullable = false, updatable = false)
    private long balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, updatable = false, length = 20)
    private ReferenceType referenceType;

    @Column(name = "reference_id", nullable = false, updatable = false)
    private UUID referenceId;

    @Column(name = "performed_by", nullable = false, updatable = false)
    private UUID performedBy;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @Column(name = "note", updatable = false, length = 500)
    private String note;
}
