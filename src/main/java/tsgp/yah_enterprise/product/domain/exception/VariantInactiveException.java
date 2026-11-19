package tsgp.yah_enterprise.product.domain.exception;

import java.util.UUID;
public class VariantInactiveException extends ProductDomainException {
    public VariantInactiveException(UUID variantId) { super("Variant is inactive: " + variantId); }
}
