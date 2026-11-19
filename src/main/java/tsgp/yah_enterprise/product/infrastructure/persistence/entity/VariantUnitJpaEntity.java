package tsgp.yah_enterprise.product.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.product.domain.model.VariantUnitStatus;

import java.time.Instant;
import java.util.UUID;

/** A unit of a variant (replaces ProductUnitJpaEntity). */
@Entity
@Table(name = "variant_units")
@Getter @Setter @NoArgsConstructor
public class VariantUnitJpaEntity {

    @Id
    @Column(name = "variant_unit_id", nullable = false, updatable = false)
    private UUID variantUnitId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false, updatable = false)
    private ProductVariantJpaEntity variant;

    @Column(name = "unit_id", nullable = false, updatable = false)
    private UUID unitId;

    @Column(name = "base_unit", nullable = false, updatable = false)
    private boolean baseUnit;

    @Column(name = "conversion_factor", nullable = false)
    private int conversionFactor;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_status", nullable = false, length = 20)
    private VariantUnitStatus unitStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}