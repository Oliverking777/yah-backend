package tsgp.yah_enterprise.product.domain.exception;
import java.util.UUID;
public class ProductUnitAlreadyAssignedException extends ProductDomainException {
    public ProductUnitAlreadyAssignedException(UUID unitId) { super("Unit already assigned to this variant: " + unitId); }
}
