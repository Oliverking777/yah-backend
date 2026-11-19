package tsgp.yah_enterprise.supplier.domain.exception;
import java.util.UUID;
public class SupplierAlreadyActiveException extends SupplierDomainException {
    public SupplierAlreadyActiveException(UUID id) { super("Supplier is already active: " + id); }
}
