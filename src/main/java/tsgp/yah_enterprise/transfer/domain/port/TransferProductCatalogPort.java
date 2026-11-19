package tsgp.yah_enterprise.transfer.domain.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Outbound: reads the Product module. A product that does not exist is an empty Optional. */
public interface TransferProductCatalogPort {

    Optional<TransferProduct> findProduct(UUID productId);

    /** One call for many products (listings). Ids that no longer resolve are left out. */
    Map<UUID, TransferProduct> findProducts(Collection<UUID> productIds);
}
