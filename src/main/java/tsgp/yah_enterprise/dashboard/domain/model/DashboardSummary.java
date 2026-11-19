package tsgp.yah_enterprise.dashboard.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A section whose source failed is null and named in unavailableSections. */
public record DashboardSummary(Instant asOf, ScopeInfo locationScope, List<String> unavailableSections, SalesSection sales, MoneySection money, StockSection stock, PendingSection pending, LogisticsSection logistics) {

    public record SalesSection(long completedSalesCountToday, long completedSalesAmountToday) { }

    public record MoneySection(long paymentsCollectedToday, long outstandingCustomerBalance) { }

    /** The three counts add up to every stock line in scope, so they can be shown as shares of a whole. */
    public record StockSection(long inStockProductCount, long lowStockProductCount, long outOfStockProductCount) { }

    public record PendingSection(long pendingOrderCount, long pendingCreditRequestCount, long pendingStockAdjustmentCount) { }

    /** Transfers sent and not yet received; purchases ordered and not yet delivered. */
    public record LogisticsSection(long pendingTransferCount, long purchaseAwaitingDeliveryCount) { }
}