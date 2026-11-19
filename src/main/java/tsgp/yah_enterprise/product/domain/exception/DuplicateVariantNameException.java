package tsgp.yah_enterprise.product.domain.exception;

public class DuplicateVariantNameException extends ProductDomainException {
    public DuplicateVariantNameException(String name) {
        super("The product already has a variant named \"" + name + "\"");
    }
}
