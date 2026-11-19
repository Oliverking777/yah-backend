package tsgp.yah_enterprise.product.domain.exception;

public class VariantSkuAlreadyExistsException extends ProductDomainException {
    public VariantSkuAlreadyExistsException(String sku) { super("SKU already exists: " + sku); }
}
