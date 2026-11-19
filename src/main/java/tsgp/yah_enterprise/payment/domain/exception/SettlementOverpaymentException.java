package tsgp.yah_enterprise.payment.domain.exception;

/** 400: the request is not valid. */
public class SettlementOverpaymentException extends PaymentDomainException {
    public SettlementOverpaymentException(String message) {
        super(message);
    }
}
