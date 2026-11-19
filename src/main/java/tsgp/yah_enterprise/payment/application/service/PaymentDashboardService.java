package tsgp.yah_enterprise.payment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.payment.application.port.in.GetPaymentDashboardFiguresUseCase;
import tsgp.yah_enterprise.payment.domain.repository.PaymentDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentDashboardService implements GetPaymentDashboardFiguresUseCase {

    private final PaymentDashboardRepository repository;

    public PaymentDashboardService(PaymentDashboardRepository repository) {
        this.repository = repository;
    }

    private static boolean none(Set<UUID> ids) {
        return ids != null && ids.isEmpty();
    }

    @Override
    public long outstandingBalance(Set<UUID> ids) {
        return none(ids) ? 0 : repository.outstandingBalance(ids);
    }

    @Override
    public long pendingCreditCount(Set<UUID> ids) {
        return none(ids) ? 0 : repository.pendingCreditCount(ids);
    }

    @Override
    public List<PaymentDashboardRepository.PendingCreditRow> pendingCredits(Set<UUID> ids, int limit) {
        return none(ids) ? List.of() : repository.pendingCredits(ids, limit);
    }
}
