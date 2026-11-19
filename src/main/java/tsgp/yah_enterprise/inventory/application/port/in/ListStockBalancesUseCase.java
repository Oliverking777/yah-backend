package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.domain.repository.StockBalancePage;

import java.util.Set;
import java.util.UUID;

/** Stock levels of a set of locations with their status. locationIds: null = every location, empty = none. */
public interface ListStockBalancesUseCase {
    StockBalancePage listStockBalances(Set<UUID> locationIds, UUID productId, String status, int page, int size);
}
