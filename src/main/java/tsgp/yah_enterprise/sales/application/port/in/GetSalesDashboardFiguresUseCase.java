package tsgp.yah_enterprise.sales.application.port.in;

import tsgp.yah_enterprise.sales.domain.repository.SalesDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Counts and short lists for the Dashboard module. Scope is decided by the caller. */
public interface GetSalesDashboardFiguresUseCase {
    long pendingOrderCount(Set<UUID> locationIds);

    SalesDashboardRepository.PendingOrderPage pendingOrders(Set<UUID> locationIds, int page, int size);
}
