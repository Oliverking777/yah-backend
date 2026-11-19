package tsgp.yah_enterprise.pricing.domain.exception;

/** Base class of every business failure of the Pricing module. */
public class PricingDomainException extends RuntimeException {
    public PricingDomainException(String message) {
        super(message);
    }
}
