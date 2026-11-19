package tsgp.yah_enterprise.product.domain.exception;
public class BaseUnitProtectedException extends ProductDomainException {
    public BaseUnitProtectedException() { super("The base unit cannot be removed or deactivated while other units exist"); }
}
