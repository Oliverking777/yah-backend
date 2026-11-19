package tsgp.yah_enterprise.sales.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only figures for reports. locationIds: null means every location, an empty set means none.
 * Only COMPLETED orders count, by completedAt in [from, to).
 */
public interface SalesReportingRepository {

    List<SalesTotalsRow> totals(Instant from, Instant to, Set<UUID> locationIds, TotalsGroupBy groupBy, String zone);

    /** Best sellers first. Ties are broken by product id (the caller may re-order by name). */
    List<TopProductRow> topProducts(Instant from, Instant to, Set<UUID> locationIds, boolean byValue, int limit);
}
