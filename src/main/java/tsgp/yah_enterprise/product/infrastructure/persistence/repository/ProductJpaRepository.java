package tsgp.yah_enterprise.product.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tsgp.yah_enterprise.product.domain.model.ProductStatus;
import tsgp.yah_enterprise.product.infrastructure.persistence.entity.ProductJpaEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    Optional<ProductJpaEntity> findByProductCode(String productCode);

    boolean existsByProductCodeIgnoreCase(String productCode);

    Page<ProductJpaEntity> findAllByProductStatus(ProductStatus status, Pageable pageable);

    long countByProductStatus(ProductStatus status);

    @Query("SELECT v.product FROM ProductVariantJpaEntity v WHERE v.variantId = :variantId")
    Optional<ProductJpaEntity> findByVariantId(@Param("variantId") UUID variantId);

    @Query("SELECT DISTINCT v.product FROM ProductVariantJpaEntity v WHERE v.variantId IN :variantIds")
    List<ProductJpaEntity> findAllByVariantIds(@Param("variantIds") Collection<UUID> variantIds);

    @Query("SELECT COUNT(v) > 0 FROM ProductVariantJpaEntity v WHERE v.sku = :sku")
    boolean existsBySku(@Param("sku") String sku);

    /** Compared upper-cased: SKUs are stored that way. */
    @Query("SELECT v.product FROM ProductVariantJpaEntity v WHERE v.sku = UPPER(:sku)")
    Optional<ProductJpaEntity> findBySku(@Param("sku") String sku);

    /** Product name or code, or the name or SKU of one of its variants. */
    @Query(value = """
            SELECT p FROM ProductJpaEntity p
            WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :term, '%'))
               OR EXISTS (SELECT 1 FROM ProductVariantJpaEntity v
                          WHERE v.product = p
                            AND (LOWER(v.variantName) LIKE LOWER(CONCAT('%', :term, '%'))
                                 OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :term, '%'))))
            ORDER BY p.productName ASC
            """,
            countQuery = """
            SELECT COUNT(p) FROM ProductJpaEntity p
            WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :term, '%'))
               OR EXISTS (SELECT 1 FROM ProductVariantJpaEntity v
                          WHERE v.product = p
                            AND (LOWER(v.variantName) LIKE LOWER(CONCAT('%', :term, '%'))
                                 OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :term, '%'))))
            """)
    Page<ProductJpaEntity> search(@Param("term") String term, Pageable pageable);
}
