package tsgp.yah_enterprise.dashboard.infrastructure.integration;

import tsgp.yah_enterprise.dashboard.domain.port.DashboardSalesPort;
import tsgp.yah_enterprise.dashboard.domain.valueobject.DashboardPeriod;
import tsgp.yah_enterprise.dashboard.domain.valueobject.LocationScope;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.sales.application.port.in.GetSalesDashboardFiguresUseCase;
import tsgp.yah_enterprise.sales.application.port.in.GetSalesTotalsUseCase;
import tsgp.yah_enterprise.sales.domain.repository.TotalsGroupBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** The dashboard reads Sales only through its use cases. "Today" uses the same definition as the reports. */
@Component
public class DashboardSalesAdapter implements DashboardSalesPort {

    private final GetSalesTotalsUseCase totals;
    private final GetSalesDashboardFiguresUseCase figures;

    public DashboardSalesAdapter(GetSalesTotalsUseCase totals, GetSalesDashboardFiguresUseCase figures) {
        this.totals = totals;
        this.figures = figures;
    }

    @Override
    public SalesToday completedToday(LocationScope scope, LocalDate day) {
        DashboardPeriod p = DashboardPeriod.day(day);
        var rows = totals.getSalesTotals(p.start(), p.end(), scope.idsOrNull(), TotalsGroupBy.NONE, p.zone().getId());
        long count = 0;
        long net = 0;
        for (var r : rows) {
            count += r.completedSalesCount();
            net += r.netAmount();
        }
        return new SalesToday(count, net);
    }

    @Override
    public List<LocationSales> salesByLocation(DashboardPeriod p, LocationScope scope) {
        return totals.getSalesTotals(p.start(), p.end(), scope.idsOrNull(), TotalsGroupBy.LOCATION,
                p.zone().getId()).stream()
                .map(r -> new LocationSales(UUID.fromString(r.key()), r.completedSalesCount(), r.netAmount()))
                .toList();
    }

    @Override
    public long pendingOrderCount(LocationScope scope) {
        return figures.pendingOrderCount(scope.idsOrNull());
    }

    @Override
    public PendingOrderPage pendingOrders(LocationScope scope, int page, int size) {
        var r = figures.pendingOrders(scope.idsOrNull(), page, size);
        return new PendingOrderPage(r.items().stream().map(o -> new PendingOrder(o.orderId(), o.orderNumber(),
                o.locationId(), o.customerId(), o.totalAmount(), o.createdAt())).toList(), r.total());
    }
}
