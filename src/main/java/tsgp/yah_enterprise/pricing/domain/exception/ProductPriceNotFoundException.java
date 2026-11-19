package tsgp.yah_enterprise.pricing.domain.exception;

import java.util.UUID;

public class ProductPriceNotFoundException extends PricingDomainException {
    public ProductPriceNotFoundException(UUID priceId) {
        super("Price not found: " + priceId);
    }
}
