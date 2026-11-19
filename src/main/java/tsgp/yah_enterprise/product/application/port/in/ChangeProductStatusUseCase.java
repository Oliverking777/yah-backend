package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.ProductResult;
import java.util.UUID;

public interface ChangeProductStatusUseCase {
    ProductResult activateProduct(UUID productId);

    /** Every variant becomes unusable with it. */
    ProductResult deactivateProduct(UUID productId);
}
