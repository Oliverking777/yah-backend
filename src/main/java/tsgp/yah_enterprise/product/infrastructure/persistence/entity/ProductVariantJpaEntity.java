package tsgp.yah_enterprise.product.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import tsgp.yah_enterprise.product.domain.model.VariantStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A variant of a product. name_key holds the name in lower case without spaces ('' when unnamed), unique per product. */
@Entity
@Table(name = "product_variants")
@Getter @Setter @NoArgsConstructor
public class ProductVariantJpaEntity {

    @Id
    @Column(name = "variant_id", nullable = false, updatable = false)
    private UUID variantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private ProductJpaEntity product;

    @Column(name = "variant_name", length = 100)
    private String variantName;

    @Column(name = "name_key", nullable = false, length = 100)
    private String nameKey;

    @Column(name = "sku", nullable = false, updatable = false, length = 30)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "variant_status", nullable = false, length = 20)
    private VariantStatus variantStatus;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("createdAt ASC")
    private List<VariantUnitJpaEntity> units = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}