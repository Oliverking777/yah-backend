package tsgp.yah_enterprise.product.domain.exception;
import java.util.UUID;
public class UnitNotAssignableException extends ProductDomainException {
    public UnitNotAssignableException(UUID unitId) { super("Unit is missing or inactive: " + unitId); }
}
