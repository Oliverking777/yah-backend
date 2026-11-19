package tsgp.yah_enterprise.payment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.payment.application.port.in.*;
import tsgp.yah_enterprise.payment.domain.exception.InvalidPaymentException;
import tsgp.yah_enterprise.payment.domain.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only payment and debt figures for the Reports and Dashboard modules. Scope is decided by the caller. */
@Service
@Transactional(readOnly = true)
public class PaymentReportingService implements GetPaymentTotalsUseCase, GetOutstandingTotalsUseCase,
        GetDebtRowsUseCase {

    private final PaymentReportingRepository reporting;

    public PaymentReportingService(PaymentReportingRepository reporting) {
        this.reporting = reporting;
    }

    @Override
    public List<AmountRow> getPaymentTotals(Instant from, Instant to, Set<UUID> locationIds, TotalsGroupBy groupBy,
                                            String zone) {
        requirePeriod(from, to);
        return none(locationIds) ? List.of() : reporting.collected(from, to, locationIds, group(groupBy), zone(zone));
    }

    @Override
    public List<AmountRow> getOutstandingTotals(Instant from, Instant to, Set<UUID> locationIds,
                                                TotalsGroupBy groupBy, String zone) {
        requirePeriod(from, to);
        return none(locationIds) ? List.of() : reporting.outstanding(from, to, locationIds, group(groupBy), zone(zone));
    }

    @Override
    public List<DebtRow> getDebtRows(Instant from, Instant to, Set<UUID> locationIds, UUID customerId,
                                     boolean onlyOutstanding) {
        requirePeriod(from, to);
        return none(locationIds) ? List.of()
                : reporting.debtRows(from, to, locationIds, customerId, onlyOutstanding);
    }

    private static boolean none(Set<UUID> locationIds) {
        return locationIds != null && locationIds.isEmpty();
    }

    private static TotalsGroupBy group(TotalsGroupBy g) {
        return g == null ? TotalsGroupBy.NONE : g;
    }

    private static String zone(String zone) {
        return zone == null ? "Africa/Douala" : zone;
    }

    private static void requirePeriod(Instant from, Instant to) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new InvalidPaymentException("A period is required: from must be before to");
        }
    }
}
