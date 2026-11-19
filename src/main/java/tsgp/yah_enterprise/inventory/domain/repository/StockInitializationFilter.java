package tsgp.yah_enterprise.inventory.domain.repository;

import tsgp.yah_enterprise.inventory.domain.model.StockInitializationStatus;

import java.util.UUID;

/** Optional filters of a stock initialization listing. A null means "any". */
public record StockInitializationFilter(StockInitializationStatus status, UUID locationId) {
}
