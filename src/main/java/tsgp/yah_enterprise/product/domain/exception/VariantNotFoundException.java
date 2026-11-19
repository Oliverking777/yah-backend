package tsgp.yah_enterprise.product.domain.exception;

import java.util.UUID;
public class VariantNotFoundException extends ProductDomainException {
    public VariantNotFoundException(UUID variantId) { super("Variant not found: " + variantId); }
}
