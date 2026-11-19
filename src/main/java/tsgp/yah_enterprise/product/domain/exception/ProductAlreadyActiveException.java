package tsgp.yah_enterprise.product.domain.exception;
import java.util.UUID;
public class ProductAlreadyActiveException extends ProductDomainException {
    public ProductAlreadyActiveException(UUID id) { super("Product is already active: " + id); }
}
