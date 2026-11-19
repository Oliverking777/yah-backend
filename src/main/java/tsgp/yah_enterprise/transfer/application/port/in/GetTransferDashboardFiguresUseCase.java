package tsgp.yah_enterprise.transfer.application.port.in;

import tsgp.yah_enterprise.transfer.domain.repository.TransferDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Counts and short lists for the Dashboard module. Scope is decided by the caller. */
public interface GetTransferDashboardFiguresUseCase {
    long pendingCount(Set<UUID> locationIds);

    TransferDashboardRepository.PendingTransferPage pending(Set<UUID> locationIds, int page, int size);

    List<TransferDashboardRepository.DiscrepancyTransferRow> discrepancyTransfers(Set<UUID> locationIds, Instant since,
                                                                                  int limit);
}
