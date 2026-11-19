package tsgp.yah_enterprise.purchasing.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Purchasing's view of the Product module. Implemented in infrastructure/integration. */
public interface PurchaseProductCatalogPort {

    Optional<PurchaseProduct> findProduct(UUID productId);

    record PurchaseProduct(UUID productId, String productName, boolean active, List<PurchaseProductUnit> units) { }

    record PurchaseProductUnit(UUID unitId, boolean active, int conversionFactor) { }
}
