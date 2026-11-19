package tsgp.yah_enterprise.reports.infrastructure.integration;

import tsgp.yah_enterprise.reports.domain.model.SalesGroupBy;
import tsgp.yah_enterprise.reports.domain.model.TopMetric;
import tsgp.yah_enterprise.reports.domain.port.ReportSalesPort;
import tsgp.yah_enterprise.reports.domain.valueobject.LocationScope;
import tsgp.yah_enterprise.reports.domain.valueobject.ReportPeriod;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.sales.application.port.in.GetSalesTotalsUseCase;
import tsgp.yah_enterprise.sales.application.port.in.GetTopProductsUseCase;
import tsgp.yah_enterprise.sales.domain.repository.TotalsGroupBy;

import java.util.List;

/** Reports reads Sales only through its use cases. */
@Component
public class ReportSalesAdapter implements ReportSalesPort {

    private final GetSalesTotalsUseCase totals;
    private final GetTopProductsUseCase top;

    public ReportSalesAdapter(GetSalesTotalsUseCase totals, GetTopProductsUseCase top) {
        this.totals = totals;
        this.top = top;
    }

    @Override
    public List<SalesTotal> salesTotals(ReportPeriod p, LocationScope scope, SalesGroupBy by) {
        return totals.getSalesTotals(p.start(), p.end(), scope.idsOrNull(), TotalsGroupBy.valueOf(by.name()),
                p.zone().getId()).stream()
                .map(r -> new SalesTotal(r.key(), r.completedSalesCount(), r.grossAmount(), r.discountAmount()))
                .toList();
    }

    @Override
    public List<TopProduct> topProducts(ReportPeriod p, LocationScope scope, TopMetric metric, int limit) {
        return top.getTopProducts(p.start(), p.end(), scope.idsOrNull(), metric == TopMetric.VALUE, limit).stream()
                .map(r -> new TopProduct(r.productId(), r.quantitySold(), r.salesValue())).toList();
    }
}
