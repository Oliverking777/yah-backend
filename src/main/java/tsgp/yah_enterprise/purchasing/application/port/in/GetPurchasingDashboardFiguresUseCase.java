package tsgp.yah_enterprise.purchasing.application.port.in;

import tsgp.yah_enterprise.purchasing.domain.repository.PurchasingDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Counts and short lists for the Dashboard module. Scope is decided by the caller. */
public interface GetPurchasingDashboardFiguresUseCase {
    /** SUBMITTED purchases: ordered, waiting for the delivery. */
    long awaitingDeliveryCount(Set<UUID> locationIds);

    List<PurchasingDashboardRepository.AwaitingDeliveryRow> awaitingDelivery(Set<UUID> locationIds, int limit);

    /** Confirmed at or after since with goods rejected or missing. */
    List<PurchasingDashboardRepository.DiscrepancyPurchaseRow> discrepancyPurchases(Set<UUID> locationIds,
                                                                                    Instant since, int limit);
}
