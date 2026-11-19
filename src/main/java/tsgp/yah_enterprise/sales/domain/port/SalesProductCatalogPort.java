package tsgp.yah_enterprise.sales.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Sales' view of the Product module. Implemented in infrastructure/integration. */
public interface SalesProductCatalogPort {

    Optional<SalesProduct> findProduct(UUID productId);

    record SalesProduct(UUID productId, String productName, boolean active, List<SalesProductUnit> units) { }

    record SalesProductUnit(UUID unitId, boolean active, int conversionFactor) { }
}
