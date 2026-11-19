package tsgp.yah_enterprise.payment.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only figures for reports. locationIds: null means every location, an empty set means none. */
public interface PaymentReportingRepository {

    /** Cash collected: payments still RECORDED, by receivedAt in [from, to), by the payment's location. */
    List<AmountRow> collected(Instant from, Instant to, Set<UUID> locationIds, TotalsGroupBy groupBy, String zone);

    /** Debt still open (receivable total minus active allocations) of the sales completed in [from, to). */
    List<AmountRow> outstanding(Instant from, Instant to, Set<UUID> locationIds, TotalsGroupBy groupBy, String zone);

    /** One row per customer and location, largest outstanding first. */
    List<DebtRow> debtRows(Instant from, Instant to, Set<UUID> locationIds, UUID customerId, boolean onlyOutstanding);
}
