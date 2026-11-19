package tsgp.yah_enterprise.reports.infrastructure.integration;

import tsgp.yah_enterprise.reports.domain.port.ReportPurchasingPort;
import tsgp.yah_enterprise.reports.domain.valueobject.LocationScope;
import tsgp.yah_enterprise.reports.domain.valueobject.ReportPeriod;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.purchasing.application.port.in.GetPurchasingTotalsUseCase;

import java.util.List;
import java.util.UUID;

/** Reports reads Purchasing only through its use case. */
@Component
public class ReportPurchasingAdapter implements ReportPurchasingPort {

    private final GetPurchasingTotalsUseCase totals;

    public ReportPurchasingAdapter(GetPurchasingTotalsUseCase totals) {
        this.totals = totals;
    }

    @Override
    public List<PurchasingTotal> purchasingTotals(ReportPeriod p, LocationScope s, UUID supplierId) {
        return totals.getPurchasingTotals(p.start(), p.end(), s.idsOrNull(), supplierId).stream()
                .map(r -> new PurchasingTotal(r.supplierId(), r.purchaseCount(), r.orderedQuantity(),
                        r.orderedAmount(), r.acceptedQuantity(), r.rejectedQuantity(), r.purchaseCost(),
                        r.discrepancyCount())).toList();
    }
}