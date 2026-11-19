package tsgp.yah_enterprise.dashboard.infrastructure.integration;

import tsgp.yah_enterprise.dashboard.domain.port.DashboardStockPort;
import tsgp.yah_enterprise.dashboard.domain.valueobject.LocationScope;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.inventory.application.port.in.GetStockDashboardFiguresUseCase;

import java.util.List;

/** The dashboard reads Inventory only through its use case. */
@Component
public class DashboardStockAdapter implements DashboardStockPort {

    private final GetStockDashboardFiguresUseCase figures;

    public DashboardStockAdapter(GetStockDashboardFiguresUseCase figures) {
        this.figures = figures;
    }

    @Override
    public StockCounts counts(LocationScope scope) {
        var c = figures.counts(scope.idsOrNull());
        return new StockCounts(c.inStockCount(), c.lowStockCount(), c.outOfStockCount());
    }

    @Override
    public List<StockAlert> lowAndOutOfStock(LocationScope scope, int limit) {
        return figures.lowAndOut(scope.idsOrNull(), limit).stream()
                .map(r -> new StockAlert(r.productId(), r.locationId(), r.quantity(), r.lowStockThreshold()))
                .toList();
    }

    @Override
    public long pendingAdjustmentCount(LocationScope scope) {
        return figures.pendingAdjustmentCount(scope.idsOrNull());
    }

    @Override
    public List<PendingAdjustment> pendingAdjustments(LocationScope scope, int limit) {
        return figures.pendingAdjustments(scope.idsOrNull(), limit).stream()
                .map(a -> new PendingAdjustment(a.adjustmentId(), a.adjustmentNumber(), a.locationId(),
                        a.createdAt())).toList();
    }
}
