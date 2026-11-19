package tsgp.yah_enterprise.sales.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.sales.application.port.in.GetSalesTotalsUseCase;
import tsgp.yah_enterprise.sales.application.port.in.GetTopProductsUseCase;
import tsgp.yah_enterprise.sales.domain.exception.InvalidOrderException;
import tsgp.yah_enterprise.sales.domain.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only sales figures for the Reports and Dashboard modules. Scope and permissions are decided by the caller. */
@Service
@Transactional(readOnly = true)
public class SalesReportingService implements GetSalesTotalsUseCase, GetTopProductsUseCase {

    private final SalesReportingRepository reporting;

    public SalesReportingService(SalesReportingRepository reporting) {
        this.reporting = reporting;
    }

    @Override
    public List<SalesTotalsRow> getSalesTotals(Instant from, Instant to, Set<UUID> locationIds, TotalsGroupBy groupBy,
                                               String zone) {
        requirePeriod(from, to);
        if (locationIds != null && locationIds.isEmpty()) {
            return List.of();
        }
        return reporting.totals(from, to, locationIds, groupBy == null ? TotalsGroupBy.NONE : groupBy,
                zone == null ? "Africa/Douala" : zone);
    }

    @Override
    public List<TopProductRow> getTopProducts(Instant from, Instant to, Set<UUID> locationIds, boolean byValue,
                                              int limit) {
        requirePeriod(from, to);
        if (limit < 1 || limit > 50) {
            throw new InvalidOrderException("The limit must be between 1 and 50");
        }
        if (locationIds != null && locationIds.isEmpty()) {
            return List.of();
        }
        return reporting.topProducts(from, to, locationIds, byValue, limit);
    }

    private static void requirePeriod(Instant from, Instant to) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new InvalidOrderException("A period is required: from must be before to");
        }
    }
}
