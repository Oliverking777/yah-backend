package tsgp.yah_enterprise.pricing.domain.exception;

import tsgp.yah_enterprise.pricing.domain.valueobject.QuantityRange;

public class PriceRangeOverlapException extends PricingDomainException {
    public PriceRangeOverlapException(QuantityRange requested) {
        super("An active price already covers quantities " + requested
                + " of this unit during the same period");
    }
}
