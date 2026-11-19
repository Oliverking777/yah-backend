package tsgp.yah_enterprise.dashboard.infrastructure.integration;

import tsgp.yah_enterprise.dashboard.domain.port.DashboardTransferPort;
import tsgp.yah_enterprise.dashboard.domain.valueobject.LocationScope;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.transfer.application.port.in.GetTransferDashboardFiguresUseCase;

import java.time.Instant;
import java.util.List;

/** The dashboard reads Transfer only through its use case. */
@Component
public class DashboardTransferAdapter implements DashboardTransferPort {

    private final GetTransferDashboardFiguresUseCase figures;

    public DashboardTransferAdapter(GetTransferDashboardFiguresUseCase figures) {
        this.figures = figures;
    }

    @Override
    public long pendingCount(LocationScope scope) {
        return figures.pendingCount(scope.idsOrNull());
    }

    @Override
    public PendingTransferPage pendingTransfers(LocationScope scope, int page, int size) {
        var r = figures.pending(scope.idsOrNull(), page, size);
        return new PendingTransferPage(r.items().stream().map(t -> new PendingTransfer(t.transferId(),
                t.transferNumber(), t.sourceLocationId(), t.destinationLocationId(), t.createdAt())).toList(),
                r.total());
    }

    @Override
    public List<DiscrepancyTransfer> discrepancyTransfers(LocationScope scope, Instant since, int limit) {
        return figures.discrepancyTransfers(scope.idsOrNull(), since, limit).stream()
                .map(d -> new DiscrepancyTransfer(d.transferId(), d.transferNumber(), d.sourceLocationId(),
                        d.destinationLocationId(), d.receivedAt())).toList();
    }
}