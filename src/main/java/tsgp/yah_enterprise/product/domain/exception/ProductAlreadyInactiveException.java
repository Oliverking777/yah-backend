package tsgp.yah_enterprise.product.domain.exception;
import java.util.UUID;
public class ProductAlreadyInactiveException extends ProductDomainException {
    public ProductAlreadyInactiveException(UUID id) { super("Product is already inactive: " + id); }
}
