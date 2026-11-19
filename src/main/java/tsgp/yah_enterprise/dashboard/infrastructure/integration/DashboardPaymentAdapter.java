package tsgp.yah_enterprise.dashboard.infrastructure.integration;

import tsgp.yah_enterprise.dashboard.domain.port.DashboardPaymentPort;
import tsgp.yah_enterprise.dashboard.domain.valueobject.DashboardPeriod;
import tsgp.yah_enterprise.dashboard.domain.valueobject.LocationScope;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.payment.application.port.in.GetPaymentDashboardFiguresUseCase;
import tsgp.yah_enterprise.payment.application.port.in.GetPaymentTotalsUseCase;
import tsgp.yah_enterprise.payment.domain.repository.TotalsGroupBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** The dashboard reads Payment only through its use cases. */
@Component
public class DashboardPaymentAdapter implements DashboardPaymentPort {

    private final GetPaymentTotalsUseCase totals;
    private final GetPaymentDashboardFiguresUseCase figures;

    public DashboardPaymentAdapter(GetPaymentTotalsUseCase totals, GetPaymentDashboardFiguresUseCase figures) {
        this.totals = totals;
        this.figures = figures;
    }

    @Override
    public long collectedToday(LocationScope scope, LocalDate day) {
        DashboardPeriod p = DashboardPeriod.day(day);
        return totals.getPaymentTotals(p.start(), p.end(), scope.idsOrNull(), TotalsGroupBy.NONE, p.zone().getId())
                .stream().mapToLong(r -> r.amount()).sum();
    }

    @Override
    public List<LocationAmount> collectedByLocation(DashboardPeriod p, LocationScope scope) {
        return totals.getPaymentTotals(p.start(), p.end(), scope.idsOrNull(), TotalsGroupBy.LOCATION,
                p.zone().getId()).stream().map(r -> new LocationAmount(UUID.fromString(r.key()), r.amount()))
                .toList();
    }

    @Override
    public long outstandingBalance(LocationScope scope) {
        return figures.outstandingBalance(scope.idsOrNull());
    }

    @Override
    public long pendingCreditCount(LocationScope scope) {
        return figures.pendingCreditCount(scope.idsOrNull());
    }

    @Override
    public List<PendingCredit> pendingCredits(LocationScope scope, int limit) {
        return figures.pendingCredits(scope.idsOrNull(), limit).stream()
                .map(c -> new PendingCredit(c.creditApprovalId(), c.saleId(), c.locationId(), c.requestedAt()))
                .toList();
    }
}
