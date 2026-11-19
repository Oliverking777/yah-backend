package tsgp.yah_enterprise.inventory.domain.repository;

import java.util.UUID;

/** Optional filters of a stock level listing. A null id means "any". */
public record StockLevelFilter(UUID locationId, UUID productId, boolean lowStockOnly) {
}
