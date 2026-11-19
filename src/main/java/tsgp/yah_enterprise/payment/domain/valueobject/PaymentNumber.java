package tsgp.yah_enterprise.payment.domain.valueobject;

import tsgp.yah_enterprise.payment.domain.exception.InvalidPaymentException;

/** The human number of a payment, PAY-000001. Generated from a sequence, never edited. */
public record PaymentNumber(String value) {

    public PaymentNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidPaymentException("Payment number is required");
        }
    }

    public static PaymentNumber of(String value) {
        return new PaymentNumber(value);
    }

    public static PaymentNumber fromSequence(long seq) {
        return new PaymentNumber("PAY-" + String.format("%06d", seq));
    }

    @Override
    public String toString() {
        return value;
    }
}
