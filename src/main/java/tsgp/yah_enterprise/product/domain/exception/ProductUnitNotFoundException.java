package tsgp.yah_enterprise.product.domain.exception;
import java.util.UUID;
public class ProductUnitNotFoundException extends ProductDomainException {
    public ProductUnitNotFoundException(UUID unitId) { super("Unit not assigned to this variant: " + unitId); }
}
