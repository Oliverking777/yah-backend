package tsgp.yah_enterprise.product.domain.repository;

import tsgp.yah_enterprise.product.domain.model.Product;
import tsgp.yah_enterprise.product.domain.model.ProductStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Loads and saves whole products, variants and their units included. */
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID productId);
    Optional<Product> findByCode(String code);
    boolean existsByCode(String code);

    /** The product that owns this variant. */
    Optional<Product> findByVariantId(UUID variantId);

    /** The products owning these variants, each once. Unknown ids are skipped. */
    List<Product> findAllByVariantIds(Collection<UUID> variantIds);

    /** True when any variant of any product has this SKU (upper case). */
    boolean existsBySku(String sku);

    /** The product whose variant has this SKU (compared in upper case). */
    Optional<Product> findBySku(String sku);

    List<Product> findAll(int page, int size, ProductStatus status);

    /** Matches the product name or code, or the name or SKU of one of its variants. */
    List<Product> search(String term, int page, int size);

    long count(ProductStatus status);
    long countSearch(String term);
}
