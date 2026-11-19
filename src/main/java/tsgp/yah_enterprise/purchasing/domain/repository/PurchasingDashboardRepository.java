package tsgp.yah_enterprise.purchasing.domain.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only dashboard figures, by the purchase's destination location.
 * locationIds: null means every location, an empty set means none.
 */
public interface PurchasingDashboardRepository {

    /** SUBMITTED purchases: ordered, waiting for the delivery. */
    long awaitingDeliveryCount(Set<UUID> locationIds);

    /** SUBMITTED purchases, the earliest expected delivery first (then the oldest order). */
    List<AwaitingDeliveryRow> awaitingDelivery(Set<UUID> locationIds, int limit);

    /** CONFIRMED at or after since with goods rejected or missing, newest first. */
    List<DiscrepancyPurchaseRow> discrepancyPurchases(Set<UUID> locationIds, Instant since, int limit);


    record AwaitingDeliveryRow(UUID purchaseId, String purchaseNumber, UUID supplierId, UUID locationId,
                               LocalDate expectedDeliveryDate, Instant createdAt) { }

    record DiscrepancyPurchaseRow(UUID purchaseId, String purchaseNumber, UUID supplierId, UUID locationId,
                                  Instant confirmedAt) { }
}
