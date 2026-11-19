package tsgp.yah_enterprise.transfer.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.transfer.application.port.in.GetInTransitStockUseCase;
import tsgp.yah_enterprise.transfer.application.port.in.GetTransferTotalsUseCase;
import tsgp.yah_enterprise.transfer.domain.exception.InvalidTransferException;
import tsgp.yah_enterprise.transfer.domain.repository.InTransitRow;
import tsgp.yah_enterprise.transfer.domain.repository.TransferReportingRepository;
import tsgp.yah_enterprise.transfer.domain.repository.TransferTotalsRow;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only transfer figures for the Reports and Dashboard modules. Scope is decided by the caller. */
@Service
@Transactional(readOnly = true)
public class TransferReportingService implements GetTransferTotalsUseCase, GetInTransitStockUseCase {

    private final TransferReportingRepository reporting;

    public TransferReportingService(TransferReportingRepository reporting) {
        this.reporting = reporting;
    }

    @Override
    public List<TransferTotalsRow> getTransferTotals(Instant from, Instant to, Set<UUID> locationIds,
                                                     UUID sourceLocationId, UUID destinationLocationId) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new InvalidTransferException("A period is required: from must be before to");
        }
        if (locationIds != null && locationIds.isEmpty()) {
            return List.of();
        }
        return reporting.totals(from, to, locationIds, sourceLocationId, destinationLocationId);
    }

    @Override
    public List<InTransitRow> getInTransitStock(Set<UUID> destinationLocationIds) {
        if (destinationLocationIds != null && destinationLocationIds.isEmpty()) {
            return List.of();
        }
        return reporting.inTransit(destinationLocationIds);
    }
}
