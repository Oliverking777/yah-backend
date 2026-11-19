package tsgp.yah_enterprise.pricing.domain.exception;

import java.util.UUID;

public class PriceAlreadyInactiveException extends PricingDomainException {
    public PriceAlreadyInactiveException(UUID priceId) {
        super("Price is already inactive: " + priceId);
    }
}
