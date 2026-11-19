package tsgp.yah_enterprise.inventory.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.inventory.application.port.in.ListStockBalancesUseCase;
import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockMovementException;
import tsgp.yah_enterprise.inventory.domain.repository.StockBalancePage;
import tsgp.yah_enterprise.inventory.domain.repository.StockReportingRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only stock figures for the Reports and Dashboard modules. Scope is decided by the caller. */
@Service
@Transactional(readOnly = true)
public class StockReportingService implements ListStockBalancesUseCase {

    private static final Set<String> STATUSES = Set.of("OUT_OF_STOCK", "LOW", "OK");

    private final StockReportingRepository reporting;

    public StockReportingService(StockReportingRepository reporting) {
        this.reporting = reporting;
    }

    @Override
    public StockBalancePage listStockBalances(Set<UUID> locationIds, UUID productId, String status, int page,
                                              int size) {
        if (status != null && !STATUSES.contains(status)) {
            throw new InvalidStockMovementException("Unknown stock status: " + status);
        }
        if (locationIds != null && locationIds.isEmpty()) {
            return new StockBalancePage(List.of(), 0);
        }
        return reporting.balances(locationIds, productId, status, Math.max(page, 0), Math.max(1, size));
    }
}
