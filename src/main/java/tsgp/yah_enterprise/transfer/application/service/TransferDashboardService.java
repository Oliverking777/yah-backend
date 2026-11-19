package tsgp.yah_enterprise.transfer.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.transfer.application.port.in.GetTransferDashboardFiguresUseCase;
import tsgp.yah_enterprise.transfer.domain.repository.TransferDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TransferDashboardService implements GetTransferDashboardFiguresUseCase {

    private final TransferDashboardRepository repository;

    public TransferDashboardService(TransferDashboardRepository repository) {
        this.repository = repository;
    }

    private static boolean none(Set<UUID> ids) {
        return ids != null && ids.isEmpty();
    }

    @Override
    public long pendingCount(Set<UUID> ids) {
        return none(ids) ? 0 : repository.pendingCount(ids);
    }

    @Override
    public TransferDashboardRepository.PendingTransferPage pending(Set<UUID> ids, int page, int size) {
        return none(ids) ? new TransferDashboardRepository.PendingTransferPage(List.of(), 0)
                : repository.pending(ids, page, size);
    }

    @Override
    public List<TransferDashboardRepository.DiscrepancyTransferRow> discrepancyTransfers(Set<UUID> ids, Instant since,
                                                                                         int limit) {
        return none(ids) ? List.of() : repository.discrepancyTransfers(ids, since, limit);
    }
}
