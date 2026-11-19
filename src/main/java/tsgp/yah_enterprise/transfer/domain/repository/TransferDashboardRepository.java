package tsgp.yah_enterprise.transfer.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only dashboard figures. locationIds: null means every location, an empty set means none. */
public interface TransferDashboardRepository {
    /** SUBMITTED, source or destination in scope. */
    long pendingCount(Set<UUID> locationIds);

    /** SUBMITTED, oldest first (by creation time). */
    PendingTransferPage pending(Set<UUID> locationIds, int page, int size);

    /** RECEIVED at or after since, with hasDiscrepancy set. */
    List<DiscrepancyTransferRow> discrepancyTransfers(Set<UUID> locationIds, Instant since, int limit);


    record PendingTransferRow(UUID transferId, String transferNumber, UUID sourceLocationId,
                              UUID destinationLocationId, Instant createdAt) { }

    record PendingTransferPage(List<PendingTransferRow> items, long total) { }

    record DiscrepancyTransferRow(UUID transferId, String transferNumber, UUID sourceLocationId,
                                  UUID destinationLocationId, Instant receivedAt) { }
}