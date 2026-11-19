package tsgp.yah_enterprise.product.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import tsgp.yah_enterprise.product.domain.model.ProductStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
public class ProductJpaEntity {

    @Id
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "product_code", nullable = false, updatable = false, length = 30)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "product_image_url", nullable = false, length = 2048)
    private String productImageUrl;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false, length = 20)
    private ProductStatus productStatus;

    /**
     * Loaded with the product. SUBSELECT (not a join) because the variants' units are a collection too: Hibernate
     * cannot join-fetch two lists at once. Oldest first; variants created together (same instant) by SKU, so
     * -01 (the one sharing the product's id) comes first.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("createdAt ASC, sku ASC")
    private List<ProductVariantJpaEntity> variants = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
