package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.ProductResult;
import java.util.UUID;

public interface ChangeVariantStatusUseCase {
    ProductResult activateVariant(UUID variantId);

    /** The last active variant of an active product cannot be deactivated: deactivate the product instead. */
    ProductResult deactivateVariant(UUID variantId);
}
