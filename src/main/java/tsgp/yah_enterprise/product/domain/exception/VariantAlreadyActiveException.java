package tsgp.yah_enterprise.product.domain.exception;

import java.util.UUID;
public class VariantAlreadyActiveException extends ProductDomainException {
    public VariantAlreadyActiveException(UUID variantId) { super("Variant is already active: " + variantId); }
}
