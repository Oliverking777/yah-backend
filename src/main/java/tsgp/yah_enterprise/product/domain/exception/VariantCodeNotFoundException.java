package tsgp.yah_enterprise.product.domain.exception;

/** No variant has this SKU. */
public class VariantCodeNotFoundException extends ProductDomainException {
    public VariantCodeNotFoundException(String code) { super("No variant has the SKU: " + code); }
}