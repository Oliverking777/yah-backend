package tsgp.yah_enterprise.customer.domain.exception;

public class WalkInCustomerProtectedException extends CustomerDomainException {
    public WalkInCustomerProtectedException() {
        super("The walk-in customer cannot be updated or deactivated");
    }
}
