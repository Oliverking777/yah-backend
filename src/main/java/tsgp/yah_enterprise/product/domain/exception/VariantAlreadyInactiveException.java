package tsgp.yah_enterprise.product.domain.exception;

import java.util.UUID;
public class VariantAlreadyInactiveException extends ProductDomainException {
    public VariantAlreadyInactiveException(UUID variantId) { super("Variant is already inactive: " + variantId); }
}
