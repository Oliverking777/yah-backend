package tsgp.yah_enterprise.product.domain.exception;
import java.util.UUID;
public class ProductInactiveException extends ProductDomainException {
    public ProductInactiveException(UUID id) { super("Product is inactive: " + id); }
}
