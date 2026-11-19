package tsgp.yah_enterprise.inventory.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only dashboard figures. locationIds: null means every location, an empty set means none. */
public interface StockDashboardRepository {
    StockCounts counts(Set<UUID> locationIds);

    /** OUT_OF_STOCK first, then LOW by smallest quantity. */
    List<StockAlertRow> lowAndOut(Set<UUID> locationIds, int limit);

    long pendingAdjustmentCount(Set<UUID> locationIds);

    List<PendingAdjustmentRow> pendingAdjustments(Set<UUID> locationIds, int limit);


    /**
     * Stock lines (one product at one location) by status. Every line is in exactly one of the three:
     * out of stock when quantity <= 0; low when 0 < quantity <= its threshold; in stock otherwise
     * (above the threshold, or no threshold set).
     */
    record StockCounts(long inStockCount, long lowStockCount, long outOfStockCount) { }

    record StockAlertRow(UUID productId, UUID locationId, long quantity, Long lowStockThreshold) { }

    record PendingAdjustmentRow(UUID adjustmentId, String adjustmentNumber, UUID locationId, Instant createdAt) { }
}