package tsgp.yah_enterprise.product.domain.exception;

import java.util.UUID;
public class LastActiveVariantException extends ProductDomainException {
    public LastActiveVariantException(UUID productId) { super("The last active variant cannot be deactivated; deactivate the product instead: " + productId); }
}
