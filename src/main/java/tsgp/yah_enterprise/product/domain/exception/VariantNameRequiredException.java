package tsgp.yah_enterprise.product.domain.exception;

/** Every variant of a product with several variants needs a name, so they can be told apart. */
public class VariantNameRequiredException extends ProductDomainException {
    public VariantNameRequiredException(String message) { super(message); }
}
