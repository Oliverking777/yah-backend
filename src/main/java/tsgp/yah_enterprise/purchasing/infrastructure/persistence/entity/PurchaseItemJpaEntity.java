package tsgp.yah_enterprise.purchasing.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.purchasing.domain.model.RejectionType;

import java.util.UUID;

@Entity
@Table(name = "purchase_items")
@Getter @Setter @NoArgsConstructor
public class PurchaseItemJpaEntity {

    @Id
    @Column(name = "purchase_item_id", nullable = false, updatable = false)
    private UUID purchaseItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false, updatable = false)
    private PurchaseJpaEntity purchase;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "unit_id", nullable = false, updatable = false)
    private UUID unitId;

    @Column(name = "conversion_factor", nullable = false, updatable = false)
    private int conversionFactor;

    @Column(name = "ordered_quantity", nullable = false)
    private int orderedQuantity;

    @Column(name = "unit_purchase_price", nullable = false)
    private long unitPurchasePrice;

    @Column(name = "line_total", nullable = false)
    private long lineTotal;

    /** Null until the purchase is confirmed. */
    @Column(name = "accepted_quantity")
    private Integer acceptedQuantity;

    @Column(name = "rejected_quantity")
    private Integer rejectedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "rejection_type", length = 20)
    private RejectionType rejectionType;

    @Column(name = "discrepancy_reason", length = 500)
    private String discrepancyReason;
}
