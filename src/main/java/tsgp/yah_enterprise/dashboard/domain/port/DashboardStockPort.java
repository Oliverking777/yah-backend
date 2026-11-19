package tsgp.yah_enterprise.dashboard.domain.port;

import tsgp.yah_enterprise.dashboard.domain.valueobject.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public interface DashboardStockPort {

    StockCounts counts(LocationScope scope);

    /** OUT_OF_STOCK first, then LOW. */
    List<StockAlert> lowAndOutOfStock(LocationScope scope, int limit);

    long pendingAdjustmentCount(LocationScope scope);

    List<PendingAdjustment> pendingAdjustments(LocationScope scope, int limit);

    /** Stock by status, counted by the same rules for all three: together they cover all stock in scope. */
    record StockCounts(long inStockCount, long lowStockCount, long outOfStockCount) { }

    record StockAlert(UUID productId, UUID locationId, long quantity, Long lowStockThreshold) { }

    record PendingAdjustment(UUID adjustmentId, String adjustmentNumber, UUID locationId, Instant since) { }
}
