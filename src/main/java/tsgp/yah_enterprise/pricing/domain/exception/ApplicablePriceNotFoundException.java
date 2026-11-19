package tsgp.yah_enterprise.pricing.domain.exception;

import java.util.UUID;

public class ApplicablePriceNotFoundException extends PricingDomainException {
    public ApplicablePriceNotFoundException(UUID productId, UUID unitId, int quantity) {
        super("No applicable price for product " + productId + ", unit " + unitId
                + " and quantity " + quantity);
    }
}
