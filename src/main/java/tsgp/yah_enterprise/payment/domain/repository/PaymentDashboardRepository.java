package tsgp.yah_enterprise.payment.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only dashboard figures. locationIds: null means every location, an empty set means none. */
public interface PaymentDashboardRepository {
    /** Receivable total minus what is allocated, over every open receivable in scope. */
    long outstandingBalance(Set<UUID> locationIds);

    /** Credit requests waiting for a decision (REQUESTED), scoped by the location of the sale. */
    long pendingCreditCount(Set<UUID> locationIds);

    List<PendingCreditRow> pendingCredits(Set<UUID> locationIds, int limit);


    record PendingCreditRow(UUID creditApprovalId, UUID saleId, UUID locationId, Instant requestedAt) { }
}
