package tsgp.yah_enterprise.sales.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.sales.application.port.in.GetSalesDashboardFiguresUseCase;
import tsgp.yah_enterprise.sales.domain.repository.SalesDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SalesDashboardService implements GetSalesDashboardFiguresUseCase {

    private final SalesDashboardRepository repository;

    public SalesDashboardService(SalesDashboardRepository repository) {
        this.repository = repository;
    }

    private static boolean none(Set<UUID> ids) {
        return ids != null && ids.isEmpty();
    }

    @Override
    public long pendingOrderCount(Set<UUID> ids) {
        return none(ids) ? 0 : repository.pendingOrderCount(ids);
    }

    @Override
    public SalesDashboardRepository.PendingOrderPage pendingOrders(Set<UUID> ids, int page, int size) {
        return none(ids) ? new SalesDashboardRepository.PendingOrderPage(List.of(), 0)
                : repository.pendingOrders(ids, page, size);
    }
}
