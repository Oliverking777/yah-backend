package tsgp.yah_enterprise.product.domain.exception;
import java.util.UUID;
public class CategoryNotAssignableException extends ProductDomainException {
    public CategoryNotAssignableException(UUID categoryId) { super("Category is missing or inactive: " + categoryId); }
}
