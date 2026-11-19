package tsgp.yah_enterprise.purchasing.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.purchasing.application.port.in.GetPurchasingDashboardFiguresUseCase;
import tsgp.yah_enterprise.purchasing.domain.repository.PurchasingDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PurchasingDashboardService implements GetPurchasingDashboardFiguresUseCase {

    private final PurchasingDashboardRepository repository;

    public PurchasingDashboardService(PurchasingDashboardRepository repository) {
        this.repository = repository;
    }

    private static boolean none(Set<UUID> ids) {
        return ids != null && ids.isEmpty();
    }

    @Override
    public long awaitingDeliveryCount(Set<UUID> ids) {
        return none(ids) ? 0 : repository.awaitingDeliveryCount(ids);
    }

    @Override
    public List<PurchasingDashboardRepository.AwaitingDeliveryRow> awaitingDelivery(Set<UUID> ids, int limit) {
        return none(ids) ? List.of() : repository.awaitingDelivery(ids, limit);
    }

    @Override
    public List<PurchasingDashboardRepository.DiscrepancyPurchaseRow> discrepancyPurchases(Set<UUID> ids,
                                                                                           Instant since, int limit) {
        return none(ids) ? List.of() : repository.discrepancyPurchases(ids, since, limit);
    }
}
