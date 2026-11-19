package tsgp.yah_enterprise.inventory.domain.repository;

import java.util.Set;
import java.util.UUID;

/** Read-only stock figures for reports. locationIds: null means every location. */
public interface StockReportingRepository {

    /** status: null, OUT_OF_STOCK, LOW or OK. Ordered by location then product. */
    StockBalancePage balances(Set<UUID> locationIds, UUID productId, String status, int page, int size);
}
