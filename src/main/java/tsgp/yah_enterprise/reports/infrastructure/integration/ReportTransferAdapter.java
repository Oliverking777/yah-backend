package tsgp.yah_enterprise.reports.infrastructure.integration;

import tsgp.yah_enterprise.reports.domain.port.ReportTransferPort;
import tsgp.yah_enterprise.reports.domain.valueobject.LocationScope;
import tsgp.yah_enterprise.reports.domain.valueobject.ReportPeriod;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.transfer.application.port.in.GetInTransitStockUseCase;
import tsgp.yah_enterprise.transfer.application.port.in.GetTransferTotalsUseCase;

import java.util.List;
import java.util.UUID;

/** Reports reads Transfer only through its use cases. */
@Component
public class ReportTransferAdapter implements ReportTransferPort {

    private final GetTransferTotalsUseCase totals;
    private final GetInTransitStockUseCase inTransit;

    public ReportTransferAdapter(GetTransferTotalsUseCase totals, GetInTransitStockUseCase inTransit) {
        this.totals = totals;
        this.inTransit = inTransit;
    }

    @Override
    public List<TransferTotal> transferTotals(ReportPeriod p, LocationScope s, UUID source, UUID destination) {
        return totals.getTransferTotals(p.start(), p.end(), s.idsOrNull(), source, destination).stream()
                .map(r -> new TransferTotal(r.sourceLocationId(), r.destinationLocationId(), r.sentQuantity(),
                        r.receivedQuantity(), r.damagedQuantity(), r.missingQuantity(), r.pendingTransferCount(),
                        r.discrepancyCount())).toList();
    }

    @Override
    public List<InTransit> inTransitByDestination(LocationScope s) {
        return inTransit.getInTransitStock(s.idsOrNull()).stream()
                .map(r -> new InTransit(r.destinationLocationId(), r.productId(), r.quantity())).toList();
    }
}