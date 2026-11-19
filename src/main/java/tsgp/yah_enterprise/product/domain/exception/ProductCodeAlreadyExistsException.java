package tsgp.yah_enterprise.product.domain.exception;
public class ProductCodeAlreadyExistsException extends ProductDomainException {
    public ProductCodeAlreadyExistsException(String code) { super("Product code already exists: " + code); }
}
