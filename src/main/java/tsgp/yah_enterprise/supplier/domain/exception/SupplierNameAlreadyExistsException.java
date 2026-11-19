package tsgp.yah_enterprise.supplier.domain.exception;
public class SupplierNameAlreadyExistsException extends SupplierDomainException {
    public SupplierNameAlreadyExistsException(String name) { super("Supplier name already exists: " + name); }
}
