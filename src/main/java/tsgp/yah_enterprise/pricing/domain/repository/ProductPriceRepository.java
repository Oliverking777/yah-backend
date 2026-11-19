package tsgp.yah_enterprise.pricing.domain.repository;

import tsgp.yah_enterprise.pricing.domain.model.ProductPrice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface (port) for product prices.
 * The implementation lives in the infrastructure layer.
 */
public interface ProductPriceRepository {

    /** Saves a new price or updates an existing one. */
    ProductPrice save(ProductPrice price);

    Optional<ProductPrice> findById(UUID priceId);

    /** All ACTIVE prices of one unit of a product, whatever their period. Used by the overlap check and the resolver. */
    List<ProductPrice> findActiveByProductAndUnit(UUID productId, UUID unitId);

    /** All ACTIVE prices of a product, every unit. */
    List<ProductPrice> findActiveByProduct(UUID productId);

    /**
     * Returns one page of every price row (active and inactive) of a product, newest first.
     *
     * @param unitId optional filter; null means every unit
     * @param page   zero-based page index
     */
    List<ProductPrice> findByProduct(UUID productId, UUID unitId, int page, int size);

    /** Total number of rows matching {@link #findByProduct}. */
    long countByProduct(UUID productId, UUID unitId);
}
