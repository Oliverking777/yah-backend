package tsgp.yah_enterprise.inventory.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_levels")
@Getter @Setter @NoArgsConstructor
public class StockLevelJpaEntity {

    @Id
    @Column(name = "stock_level_id", nullable = false, updatable = false)
    private UUID stockLevelId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Column(name = "low_stock_threshold")
    private Long lowStockThreshold;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
