package tsgp.yah_enterprise.sales.application.service;

/** Hands out the next number of the receipt sequence (RCT-000001...). Implemented in the infrastructure layer. */
public interface ReceiptNumberSequence {
    long next();
}
