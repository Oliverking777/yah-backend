package tsgp.yah_enterprise.supplier.domain.exception;
import java.util.UUID;
public class SupplierAlreadyInactiveException extends SupplierDomainException {
    public SupplierAlreadyInactiveException(UUID id) { super("Supplier is already inactive: " + id); }
}
