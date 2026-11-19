package tsgp.yah_enterprise.purchasing.domain.exception;

public class SelfConfirmationNotAllowedException extends PurchasingDomainException {
    public SelfConfirmationNotAllowedException(String message) {
        super(message);
    }
}
