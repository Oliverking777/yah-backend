package tsgp.yah_enterprise.supplier.domain.exception;
import java.util.UUID;
public class SupplierInactiveException extends SupplierDomainException {
    public SupplierInactiveException(UUID id) { super("Supplier is inactive: " + id); }
}
