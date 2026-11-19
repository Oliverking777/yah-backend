package tsgp.yah_enterprise.transfer.application.service;

/** Hands out the next number of the transfer sequence (TRF-000001...). Implemented in the infrastructure layer. */
public interface TransferNumberSequence {
    long next();
}
