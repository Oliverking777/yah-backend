package tsgp.yah_enterprise.payment.domain.exception;

/** 400: the request is not valid. */
public class InvalidCreditRequestException extends PaymentDomainException {
    public InvalidCreditRequestException(String message) {
        super(message);
    }
}
