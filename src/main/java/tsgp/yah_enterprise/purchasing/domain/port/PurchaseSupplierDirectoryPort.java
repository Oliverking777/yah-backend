package tsgp.yah_enterprise.purchasing.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Purchasing's view of the Supplier module. Implemented in infrastructure/integration. */
public interface PurchaseSupplierDirectoryPort {

    Optional<PurchaseSupplier> findById(UUID supplierId);

    List<PurchaseSupplier> findAllByIds(Collection<UUID> supplierIds);

    record PurchaseSupplier(UUID supplierId, String supplierName, boolean active) { }
}
