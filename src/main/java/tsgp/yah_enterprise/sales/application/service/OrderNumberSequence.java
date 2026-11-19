package tsgp.yah_enterprise.sales.application.service;

/** Hands out the next number of the order sequence (ORD-000001...). Implemented in the infrastructure layer. */
public interface OrderNumberSequence {
    long next();
}
