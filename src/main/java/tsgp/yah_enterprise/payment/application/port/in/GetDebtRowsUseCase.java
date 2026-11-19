package tsgp.yah_enterprise.payment.application.port.in;

import tsgp.yah_enterprise.payment.domain.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only. locationIds: null = every location, empty = none. */
public interface GetDebtRowsUseCase {
    List<DebtRow> getDebtRows(Instant from, Instant to, Set<UUID> locationIds, UUID customerId, boolean onlyOutstanding);
}
