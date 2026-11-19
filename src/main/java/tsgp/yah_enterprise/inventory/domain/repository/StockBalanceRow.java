package tsgp.yah_enterprise.inventory.domain.repository;

import java.util.UUID;

/** status is OUT_OF_STOCK (quantity 0), LOW (quantity at or under the threshold) or OK. */
public record StockBalanceRow(UUID productId, UUID locationId, long quantity, Long lowStockThreshold, String status) { }
