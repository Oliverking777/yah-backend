package tsgp.yah_enterprise.product.domain.exception;
import java.util.UUID;
public class ProductNotFoundException extends ProductDomainException {
    public ProductNotFoundException(UUID productId) { super("Product not found: " + productId); }
}
