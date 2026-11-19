package tsgp.yah_enterprise.dashboard.application.service;

import tsgp.yah_enterprise.dashboard.application.dto.DashboardActor;
import tsgp.yah_enterprise.dashboard.application.port.in.*;
import tsgp.yah_enterprise.dashboard.domain.exception.InvalidDashboardRequestException;
import tsgp.yah_enterprise.dashboard.domain.model.*;
import tsgp.yah_enterprise.dashboard.domain.port.*;
import tsgp.yah_enterprise.dashboard.domain.valueobject.DashboardPeriod;
import tsgp.yah_enterprise.dashboard.domain.valueobject.LocationScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Summary, sales by location, pending lists and recent activity. Read-only. The summary never fails because of one
 * source: that section is null and named in unavailableSections. The list endpoints fail with 503 instead.
 */
@Service
public class DashboardService implements GetDashboardSummaryUseCase, GetSalesByLocationUseCase,
        GetPendingOrdersUseCase, GetPendingTransfersUseCase, GetRecentActivityUseCase {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_ACTIVITY_LIMIT = 20;
    public static final int MAX_ACTIVITY_LIMIT = 50;

    private final DashboardSalesPort sales;
    private final DashboardPaymentPort payments;
    private final DashboardStockPort stock;
    private final DashboardTransferPort transfers;
    private final DashboardPurchasingPort purchasing;
    private final DashboardActivityPort activity;
    private final DashboardSupport support;
    private final Clock clock;

    @Autowired
    public DashboardService(DashboardSalesPort sales, DashboardPaymentPort payments, DashboardStockPort stock,
                            DashboardTransferPort transfers, DashboardPurchasingPort purchasing,
                            DashboardActivityPort activity, DashboardLocationScopePort locations,
                            SectionExecutor executor) {
        this(sales, payments, stock, transfers, purchasing, activity, locations, executor, Clock.systemUTC());
    }

    public DashboardService(DashboardSalesPort sales, DashboardPaymentPort payments, DashboardStockPort stock,
                            DashboardTransferPort transfers, DashboardPurchasingPort purchasing,
                            DashboardActivityPort activity, DashboardLocationScopePort locations,
                            SectionExecutor executor, Clock clock) {
        this.sales = sales;
        this.payments = payments;
        this.stock = stock;
        this.transfers = transfers;
        this.purchasing = purchasing;
        this.activity = activity;
        this.support = new DashboardSupport(locations, executor);
        this.clock = clock;
    }

    private LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), DashboardPeriod.BUSINESS_ZONE);
    }

    // -------------------------------------------------------------- summary

    @Override
    public DashboardSummary getDashboardSummary(DashboardActor actor, UUID locationId) {
        LocationScope scope = support.scope(actor, locationId);
        Instant asOf = clock.instant();
        LocalDate day = today();
        long deadline = support.deadline();
        Future<DashboardSummary.SalesSection> fSales = support.submit(() -> {
            DashboardSalesPort.SalesToday t = sales.completedToday(scope, day);
            return new DashboardSummary.SalesSection(t.completedSalesCount(), t.netAmount());
        });
        Future<DashboardSummary.MoneySection> fMoney = support.submit(() ->
                new DashboardSummary.MoneySection(payments.collectedToday(scope, day),
                        payments.outstandingBalance(scope)));
        Future<DashboardSummary.StockSection> fStock = support.submit(() -> {
            DashboardStockPort.StockCounts c = stock.counts(scope);
            return new DashboardSummary.StockSection(c.inStockCount(), c.lowStockCount(), c.outOfStockCount());
        });
        Future<DashboardSummary.PendingSection> fPending = support.submit(() ->
                new DashboardSummary.PendingSection(sales.pendingOrderCount(scope),
                        payments.pendingCreditCount(scope), stock.pendingAdjustmentCount(scope)));
        Future<DashboardSummary.LogisticsSection> fLogistics = support.submit(() ->
                new DashboardSummary.LogisticsSection(transfers.pendingCount(scope),
                        purchasing.awaitingDeliveryCount(scope)));
        List<String> unavailable = new ArrayList<>();
        DashboardSummary.SalesSection s = support.await(fSales, deadline);
        if (s == null) {
            unavailable.add("sales");
        }
        DashboardSummary.MoneySection m = support.await(fMoney, deadline);
        if (m == null) {
            unavailable.add("money");
        }
        DashboardSummary.StockSection st = support.await(fStock, deadline);
        if (st == null) {
            unavailable.add("stock");
        }
        DashboardSummary.PendingSection p = support.await(fPending, deadline);
        if (p == null) {
            unavailable.add("pending");
        }
        DashboardSummary.LogisticsSection l = support.await(fLogistics, deadline);
        if (l == null) {
            unavailable.add("logistics");
        }
        return new DashboardSummary(asOf, support.scopeInfo(scope), List.copyOf(unavailable), s, m, st, p, l);
    }

    // ---------------------------------------------------- sales by location

    @Override
    public List<LocationSalesSummary> getSalesByLocation(DashboardActor actor, LocalDate from, LocalDate to) {
        LocalDate t = today();
        DashboardPeriod period = DashboardPeriod.of(from == null ? (to == null ? t : to) : from,
                to == null ? (from == null ? t : from) : to);
        LocationScope scope = support.scope(actor, null);
        if (scope.isEmpty()) {
            return List.of();
        }
        List<DashboardSalesPort.LocationSales> soldRows = DashboardSupport.required(
                () -> sales.salesByLocation(period, scope));
        List<DashboardPaymentPort.LocationAmount> collectedRows = DashboardSupport.required(
                () -> payments.collectedByLocation(period, scope));
        Map<UUID, long[]> merged = new LinkedHashMap<>();
        for (DashboardSalesPort.LocationSales r : soldRows) {
            long[] v = merged.computeIfAbsent(r.locationId(), k -> new long[3]);
            v[0] += r.completedSalesCount();
            v[1] += r.netAmount();
        }
        for (DashboardPaymentPort.LocationAmount r : collectedRows) {
            merged.computeIfAbsent(r.locationId(), k -> new long[3])[2] += r.amount();
        }
        Map<UUID, String> names = support.names(merged.keySet());
        List<LocationSalesSummary> out = new ArrayList<>();
        for (Map.Entry<UUID, long[]> e : merged.entrySet()) {
            out.add(new LocationSalesSummary(e.getKey(), names.get(e.getKey()), e.getValue()[0], e.getValue()[1],
                    e.getValue()[2]));
        }
        out.sort(Comparator.comparing(LocationSalesSummary::locationName,
                Comparator.nullsLast(String::compareToIgnoreCase)).thenComparing(LocationSalesSummary::locationId));
        return List.copyOf(out);
    }

    // ------------------------------------------------------- pending lists

    @Override
    public OrderPendingPage getPendingOrders(DashboardActor actor, UUID locationId, int page, int size) {
        checkPage(page, size);
        LocationScope scope = support.scope(actor, locationId);
        if (scope.isEmpty()) {
            return new OrderPendingPage(List.of(), page, size, 0, 0);
        }
        DashboardSalesPort.PendingOrderPage r = DashboardSupport.required(() -> sales.pendingOrders(scope, page, size));
        Map<UUID, String> names = support.names(r.items().stream().map(DashboardSalesPort.PendingOrder::locationId).toList());
        List<OrderPendingPage.Item> items = r.items().stream().map(o -> new OrderPendingPage.Item(o.orderId(),
                o.orderNumber(), o.locationId(), names.get(o.locationId()), o.customerId(), o.totalAmount(),
                o.createdAt())).toList();
        return new OrderPendingPage(items, page, size, r.total(), DashboardSupport.pages(r.total(), size));
    }

    @Override
    public TransferPendingPage getPendingTransfers(DashboardActor actor, UUID locationId, int page, int size) {
        checkPage(page, size);
        LocationScope scope = support.scope(actor, locationId);
        if (scope.isEmpty()) {
            return new TransferPendingPage(List.of(), page, size, 0, 0);
        }
        DashboardTransferPort.PendingTransferPage r = DashboardSupport.required(
                () -> transfers.pendingTransfers(scope, page, size));
        Set<UUID> ids = new HashSet<>();
        for (DashboardTransferPort.PendingTransfer t : r.items()) {
            ids.add(t.sourceLocationId());
            ids.add(t.destinationLocationId());
        }
        Map<UUID, String> names = support.names(ids);
        // every pending transfer has been sent and waits for the destination to receive it
        List<TransferPendingPage.Item> items = r.items().stream().map(t -> new TransferPendingPage.Item(
                t.transferId(), t.transferNumber(), "AWAITING_RECEIPT", t.sourceLocationId(),
                names.get(t.sourceLocationId()), t.destinationLocationId(), names.get(t.destinationLocationId()),
                t.sentAt())).toList();
        return new TransferPendingPage(items, page, size, r.total(), DashboardSupport.pages(r.total(), size));
    }

    private static void checkPage(int page, int size) {
        if (page < 0) {
            throw new InvalidDashboardRequestException("page cannot be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidDashboardRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    // ------------------------------------------------------- recent activity

    @Override
    public List<RecentActivityItem> getRecentActivity(DashboardActor actor, Integer limit) {
        int n = limit == null ? DEFAULT_ACTIVITY_LIMIT : limit;
        if (n < 1 || n > MAX_ACTIVITY_LIMIT) {
            throw new InvalidDashboardRequestException("limit must be between 1 and " + MAX_ACTIVITY_LIMIT);
        }
        LocationScope scope = support.scope(actor, null);
        if (scope.isEmpty()) {
            return List.of();
        }
        return DashboardSupport.required(() -> activity.recent(scope, n)).stream()
                .map(a -> new RecentActivityItem(a.occurredAt(), a.actorId(), a.action(), a.entityType(),
                        a.entityId(), a.locationId(), a.outcome())).toList();
    }
}