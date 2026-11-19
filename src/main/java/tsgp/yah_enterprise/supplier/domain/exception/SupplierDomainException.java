package tsgp.yah_enterprise.supplier.domain.exception;

public abstract class SupplierDomainException extends RuntimeException {
    protected SupplierDomainException(String message) { super(message); }
}
