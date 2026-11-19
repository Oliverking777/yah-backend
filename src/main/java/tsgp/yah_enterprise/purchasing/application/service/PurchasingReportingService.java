package tsgp.yah_enterprise.purchasing.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.purchasing.application.port.in.GetPurchasingTotalsUseCase;
import tsgp.yah_enterprise.purchasing.domain.exception.InvalidPurchaseException;
import tsgp.yah_enterprise.purchasing.domain.repository.PurchasingReportingRepository;
import tsgp.yah_enterprise.purchasing.domain.repository.PurchasingTotalsRow;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only purchasing figures for the Reports and Dashboard modules. Scope is decided by the caller. */
@Service
@Transactional(readOnly = true)
public class PurchasingReportingService implements GetPurchasingTotalsUseCase {

    private final PurchasingReportingRepository reporting;

    public PurchasingReportingService(PurchasingReportingRepository reporting) {
        this.reporting = reporting;
    }

    @Override
    public List<PurchasingTotalsRow> getPurchasingTotals(Instant from, Instant to, Set<UUID> locationIds,
                                                         UUID supplierId) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new InvalidPurchaseException("A period is required: from must be before to");
        }
        if (locationIds != null && locationIds.isEmpty()) {
            return List.of();
        }
        return reporting.totals(from, to, locationIds, supplierId);
    }
}
