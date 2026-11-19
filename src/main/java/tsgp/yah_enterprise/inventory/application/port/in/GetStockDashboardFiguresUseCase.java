package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.domain.repository.StockDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Counts and short lists for the Dashboard module. Scope is decided by the caller. */
public interface GetStockDashboardFiguresUseCase {
    StockDashboardRepository.StockCounts counts(Set<UUID> locationIds);

    List<StockDashboardRepository.StockAlertRow> lowAndOut(Set<UUID> locationIds, int limit);

    long pendingAdjustmentCount(Set<UUID> locationIds);

    List<StockDashboardRepository.PendingAdjustmentRow> pendingAdjustments(Set<UUID> locationIds, int limit);
}
