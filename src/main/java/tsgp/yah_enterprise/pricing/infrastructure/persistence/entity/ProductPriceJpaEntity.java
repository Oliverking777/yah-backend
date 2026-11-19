package tsgp.yah_enterprise.pricing.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.pricing.domain.model.PriceStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence representation of a ProductPrice. Never leaves the infrastructure layer.
 * productId and unitId belong to the Product module: they are plain ids with no foreign key.
 * "No two ACTIVE prices overlap" is guaranteed by the exclusion constraint
 * ex_product_prices_no_overlap created in the V11 migration (JPA cannot declare it).
 */
@Entity
@Table(name = "product_prices")
@Getter
@Setter
@NoArgsConstructor
public class ProductPriceJpaEntity {

    @Id
    @Column(name = "price_id", nullable = false, updatable = false)
    private UUID priceId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "unit_id", nullable = false, updatable = false)
    private UUID unitId;

    @Column(name = "price", nullable = false, updatable = false)
    private long price;

    @Column(name = "minimum_quantity", nullable = false, updatable = false)
    private int minimumQuantity;

    @Column(name = "maximum_quantity", updatable = false)
    private Integer maximumQuantity;

    @Column(name = "effective_from", nullable = false, updatable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_status", nullable = false, length = 20)
    private PriceStatus priceStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
