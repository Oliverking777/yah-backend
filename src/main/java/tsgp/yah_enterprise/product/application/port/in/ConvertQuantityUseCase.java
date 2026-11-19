package tsgp.yah_enterprise.product.application.port.in;

import java.util.UUID;

/**
 * Converts a quantity between two units of a variant; refused when it does not come out whole.
 * A module still keyed by product id may pass it: a product's first variant has the product's id.
 */
public interface ConvertQuantityUseCase { long convertQuantity(UUID variantId, UUID fromUnitId, UUID toUnitId, long quantity); }
