package tsgp.yah_enterprise.dashboard.infrastructure.integration;

import tsgp.yah_enterprise.dashboard.domain.port.DashboardPurchasingPort;
import tsgp.yah_enterprise.dashboard.domain.valueobject.LocationScope;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.purchasing.application.port.in.GetPurchasingDashboardFiguresUseCase;

import java.time.Instant;
import java.util.List;

/** The dashboard reads Purchasing only through its use case. */
@Component
public class DashboardPurchasingAdapter implements DashboardPurchasingPort {

    private final GetPurchasingDashboardFiguresUseCase figures;

    public DashboardPurchasingAdapter(GetPurchasingDashboardFiguresUseCase figures) {
        this.figures = figures;
    }

    @Override
    public long awaitingDeliveryCount(LocationScope scope) {
        return figures.awaitingDeliveryCount(scope.idsOrNull());
    }

    @Override
    public List<AwaitingDelivery> awaitingDelivery(LocationScope scope, int limit) {
        return figures.awaitingDelivery(scope.idsOrNull(), limit).stream()
                .map(r -> new AwaitingDelivery(r.purchaseId(), r.purchaseNumber(), r.locationId(),
                        r.expectedDeliveryDate(), r.createdAt()))
                .toList();
    }

    @Override
    public List<DiscrepancyPurchase> discrepancyPurchases(LocationScope scope, Instant since, int limit) {
        return figures.discrepancyPurchases(scope.idsOrNull(), since, limit).stream()
                .map(r -> new DiscrepancyPurchase(r.purchaseId(), r.purchaseNumber(), r.locationId(),
                        r.confirmedAt()))
                .toList();
    }
}