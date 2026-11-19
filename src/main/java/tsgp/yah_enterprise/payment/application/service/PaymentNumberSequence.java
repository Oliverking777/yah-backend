package tsgp.yah_enterprise.payment.application.service;

/** Hands out the next number of the payment sequence (PAY-000001...). Implemented in the infrastructure layer. */
public interface PaymentNumberSequence {
    long next();
}
