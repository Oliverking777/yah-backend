package tsgp.yah_enterprise.sales.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only dashboard figures. locationIds: null means every location, an empty set means none. */
public interface SalesDashboardRepository {
    long pendingOrderCount(Set<UUID> locationIds);

    /** Orders waiting for validation (PENDING), oldest first. */
    PendingOrderPage pendingOrders(Set<UUID> locationIds, int page, int size);


    record PendingOrderRow(UUID orderId, String orderNumber, UUID locationId, UUID customerId, long totalAmount,
                           Instant createdAt) { }

    record PendingOrderPage(List<PendingOrderRow> items, long total) { }
}
