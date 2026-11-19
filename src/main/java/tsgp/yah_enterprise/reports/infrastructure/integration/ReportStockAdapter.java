package tsgp.yah_enterprise.reports.infrastructure.integration;

import tsgp.yah_enterprise.reports.domain.model.StockStatus;
import tsgp.yah_enterprise.reports.domain.port.ReportStockPort;
import tsgp.yah_enterprise.reports.domain.valueobject.LocationScope;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.inventory.application.port.in.ListStockBalancesUseCase;

import java.util.UUID;

/** Reports reads Inventory only through its use case. */
@Component
public class ReportStockAdapter implements ReportStockPort {

    private final ListStockBalancesUseCase balances;

    public ReportStockAdapter(ListStockBalancesUseCase balances) {
        this.balances = balances;
    }

    @Override
    public StockBalancePage balances(LocationScope scope, UUID productId, StockStatus status, int page, int size) {
        var r = balances.listStockBalances(scope.idsOrNull(), productId, status == null ? null : status.name(),
                page, size);
        return new StockBalancePage(r.items().stream().map(b -> new StockBalance(b.productId(), b.locationId(),
                b.quantity(), b.lowStockThreshold())).toList(), r.total());
    }
}
