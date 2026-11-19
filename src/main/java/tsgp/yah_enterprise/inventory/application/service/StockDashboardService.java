package tsgp.yah_enterprise.inventory.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.inventory.application.port.in.GetStockDashboardFiguresUseCase;
import tsgp.yah_enterprise.inventory.domain.repository.StockDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StockDashboardService implements GetStockDashboardFiguresUseCase {

    private final StockDashboardRepository repository;

    public StockDashboardService(StockDashboardRepository repository) {
        this.repository = repository;
    }

    private static boolean none(Set<UUID> ids) {
        return ids != null && ids.isEmpty();
    }

    @Override
    public StockDashboardRepository.StockCounts counts(Set<UUID> ids) {
        return none(ids) ? new StockDashboardRepository.StockCounts(0, 0, 0) : repository.counts(ids);
    }

    @Override
    public List<StockDashboardRepository.StockAlertRow> lowAndOut(Set<UUID> ids, int limit) {
        return none(ids) ? List.of() : repository.lowAndOut(ids, limit);
    }

    @Override
    public long pendingAdjustmentCount(Set<UUID> ids) {
        return none(ids) ? 0 : repository.pendingAdjustmentCount(ids);
    }

    @Override
    public List<StockDashboardRepository.PendingAdjustmentRow> pendingAdjustments(Set<UUID> ids, int limit) {
        return none(ids) ? List.of() : repository.pendingAdjustments(ids, limit);
    }
}