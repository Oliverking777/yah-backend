package tsgp.yah_enterprise.transfer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "transfer_items")
@Getter @Setter @NoArgsConstructor
public class TransferItemJpaEntity {

    @Id
    @Column(name = "transfer_item_id", nullable = false, updatable = false)
    private UUID transferItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_id", nullable = false, updatable = false)
    private TransferJpaEntity transfer;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "unit_id", nullable = false, updatable = false)
    private UUID unitId;

    @Column(name = "conversion_factor", nullable = false, updatable = false)
    private int conversionFactor;

    /** Quantity sent, in the line's unit. */
    @Column(name = "quantity", nullable = false, updatable = false)
    private int quantity;

    @Column(name = "received_quantity")
    private Integer receivedQuantity;

    @Column(name = "damaged_quantity")
    private Integer damagedQuantity;

    @Column(name = "discrepancy_reason", length = 500)
    private String discrepancyReason;
}