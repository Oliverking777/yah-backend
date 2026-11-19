package tsgp.yah_enterprise.pricing.domain.exception;

/** The product is missing or inactive, or the unit does not belong to it or is inactive. */
public class PriceProductNotPriceableException extends PricingDomainException {
    public PriceProductNotPriceableException(String reason) {
        super(reason);
    }
}
