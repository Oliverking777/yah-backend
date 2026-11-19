package tsgp.yah_enterprise.product.domain.exception;

public class VariantBarcodeAlreadyExistsException extends ProductDomainException {
    public VariantBarcodeAlreadyExistsException(String barcode) { super("Barcode already used by another variant: " + barcode); }
}
