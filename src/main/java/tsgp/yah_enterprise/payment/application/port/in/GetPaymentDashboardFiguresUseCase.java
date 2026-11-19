package tsgp.yah_enterprise.payment.application.port.in;

import tsgp.yah_enterprise.payment.domain.repository.PaymentDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Counts and short lists for the Dashboard module. Scope is decided by the caller. */
public interface GetPaymentDashboardFiguresUseCase {
    long outstandingBalance(Set<UUID> locationIds);

    long pendingCreditCount(Set<UUID> locationIds);

    List<PaymentDashboardRepository.PendingCreditRow> pendingCredits(Set<UUID> locationIds, int limit);
}
