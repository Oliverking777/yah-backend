package tsgp.yah_enterprise.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * One product line of a stock initialization.
 * <p>
 * location_id is denormalized from the parent so the unique constraint
 * (location_id, product_id) can prevent a product from being initialized twice at the same location.
 */
@Entity
@Table(name = "stock_initialization_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_stock_init_items_init_product", columnNames = {"initialization_id", "product_id"}),
                @UniqueConstraint(name = "uq_stock_init_items_location_product", columnNames = {"location_id", "product_id"})
        })
@Getter @Setter @NoArgsConstructor
public class StockInitializationItemJpaEntity {

    @Id
    @Column(name = "item_id", nullable = false, updatable = false)
    private UUID itemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initialization_id", nullable = false, updatable = false)
    private StockInitializationJpaEntity initialization;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    /** Denormalized from the parent for the unique constraint. */
    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Column(name = "unit_id", nullable = false, updatable = false)
    private UUID unitId;

    @Column(name = "conversion_factor", nullable = false, updatable = false)
    private int conversionFactor;

    @Column(name = "counted_quantity", nullable = false, updatable = false)
    private long countedQuantity;

    @Column(name = "base_quantity", nullable = false, updatable = false)
    private long baseQuantity;
}
