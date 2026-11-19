package tsgp.yah_enterprise.sales.domain.port;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Sales' view of the Pricing module. Empty when the unit has no price for the quantity and moment. */
public interface SalesPricingPort {

    Optional<SalePrice> applicablePrice(UUID productId, UUID unitId, int quantity, Instant at);

    record SalePrice(UUID priceId, long unitPrice) { }
}
