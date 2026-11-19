package tsgp.yah_enterprise.purchasing.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only figures for reports. locationIds (destination locations): null means every location, empty means none. */
public interface PurchasingReportingRepository {

    List<PurchasingTotalsRow> totals(Instant from, Instant to, Set<UUID> locationIds, UUID supplierId);
}
