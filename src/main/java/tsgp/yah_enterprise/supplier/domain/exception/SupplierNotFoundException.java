package tsgp.yah_enterprise.supplier.domain.exception;
import java.util.UUID;
public class SupplierNotFoundException extends SupplierDomainException {
    public SupplierNotFoundException(UUID supplierId) { super("Supplier not found: " + supplierId); }
}
