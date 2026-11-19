package tsgp.yah_enterprise.sales.application.port.in;

import tsgp.yah_enterprise.sales.domain.repository.SalesTotalsRow;
import tsgp.yah_enterprise.sales.domain.repository.TotalsGroupBy;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Completed-sales count, gross, discount and net per group. locationIds: null = all, empty = none. */
public interface GetSalesTotalsUseCase {
    List<SalesTotalsRow> getSalesTotals(Instant from, Instant to, Set<UUID> locationIds, TotalsGroupBy groupBy,
                                        String zone);
}
