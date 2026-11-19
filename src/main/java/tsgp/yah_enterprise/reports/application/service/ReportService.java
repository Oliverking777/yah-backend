package tsgp.yah_enterprise.reports.application.service;

import tsgp.yah_enterprise.reports.application.dto.ReportActor;
import tsgp.yah_enterprise.reports.application.port.in.*;
import tsgp.yah_enterprise.reports.domain.exception.InvalidReportRequestException;
import tsgp.yah_enterprise.reports.domain.exception.ReportSourceUnavailableException;
import tsgp.yah_enterprise.reports.domain.exception.ReportsDomainException;
import tsgp.yah_enterprise.reports.domain.model.*;
import tsgp.yah_enterprise.reports.domain.port.*;
import tsgp.yah_enterprise.reports.domain.service.LocationScopePolicy;
import tsgp.yah_enterprise.reports.domain.service.StockStatusPolicy;
import tsgp.yah_enterprise.reports.domain.valueobject.LocationScope;
import tsgp.yah_enterprise.reports.domain.valueobject.ReportPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;

/**
 * The six reports. Read-only. The location scope is decided here from the caller's permissions, never from the
 * request. If any source module fails the whole report fails (503): a partial report would be read as complete.
 */
@Service
@Transactional(readOnly = true)
public class ReportService implements GetSalesReportUseCase, GetStockReportUseCase, GetPurchasingReportUseCase,
        GetTransferReportUseCase, GetCustomerDebtReportUseCase, GetTopSellingProductsReportUseCase {

    public static final int MAX_PAGE_SIZE = 200;
    public static final int DEFAULT_TOP_LIMIT = 10;
    public static final int MAX_TOP_LIMIT = 50;
    static final String TOTAL_KEY = "TOTAL";

    private final ReportSalesPort sales;
    private final ReportPaymentPort payments;
    private final ReportStockPort stock;
    private final ReportTransferPort transfers;
    private final ReportPurchasingPort purchasing;
    private final ReportLocationScopePort locations;
    private final ReportNameDirectoryPort names;
    private final LocationScopePolicy scopePolicy = new LocationScopePolicy();
    private final StockStatusPolicy stockPolicy = new StockStatusPolicy();

    @Autowired
    public ReportService(ReportSalesPort sales, ReportPaymentPort payments, ReportStockPort stock,
                         ReportTransferPort transfers, ReportPurchasingPort purchasing,
                         ReportLocationScopePort locations, ReportNameDirectoryPort names) {
        this.sales = sales;
        this.payments = payments;
        this.stock = stock;
        this.transfers = transfers;
        this.purchasing = purchasing;
        this.locations = locations;
        this.names = names;
    }

    // ---------------------------------------------------------------- sales

    @Override
    public SalesReport getSalesReport(ReportActor actor, LocalDate from, LocalDate to, UUID locationId,
                                      SalesGroupBy groupBy) {
        ReportPeriod period = ReportPeriod.of(from, to);
        SalesGroupBy by = groupBy == null ? SalesGroupBy.NONE : groupBy;
        LocationScope scope = scope(actor, locationId);
        Instant asOf = Instant.now();
        Map<String, long[]> merged = new TreeMap<>();
        if (!scope.isEmpty()) {
            for (ReportSalesPort.SalesTotal t : source(() -> sales.salesTotals(period, scope, by))) {
                long[] v = merged.computeIfAbsent(t.key(), k -> new long[6]);
                v[0] += t.completedSalesCount();
                v[1] += t.grossAmount();
                v[2] += t.discountAmount();
            }
            for (ReportPaymentPort.AmountByKey a : source(() -> payments.collectedTotals(period, scope, by))) {
                merged.computeIfAbsent(a.key(), k -> new long[6])[4] += a.amount();
            }
            for (ReportPaymentPort.AmountByKey a : source(() -> payments.outstandingTotals(period, scope, by))) {
                merged.computeIfAbsent(a.key(), k -> new long[6])[5] += a.amount();
            }
        }
        if (by == SalesGroupBy.NONE) {
            merged.computeIfAbsent("ALL", k -> new long[6]);
        }
        Map<UUID, String> locationNames = Map.of();
        if (by == SalesGroupBy.LOCATION) {
            locationNames = source(() -> locations.locationNames(uuidKeys(merged.keySet())));
        }
        List<SalesReportRow> rows = new ArrayList<>();
        for (Map.Entry<String, long[]> e : merged.entrySet()) {
            rows.add(salesRow(e.getKey(), labelOf(by, e.getKey(), locationNames), e.getValue()));
        }
        if (by == SalesGroupBy.LOCATION) {
            rows.sort(Comparator.comparing(SalesReportRow::groupLabel, Comparator.nullsLast(String::compareToIgnoreCase))
                    .thenComparing(SalesReportRow::groupKey));
        }
        return new SalesReport(from, to, asOf, scopeOf(scope), by, List.copyOf(rows), sumSales(rows));
    }

    private static SalesReportRow salesRow(String key, String label, long[] v) {
        return new SalesReportRow(key, label, v[0], v[1], v[2], v[1] - v[2], v[4], v[5]);
    }

    private static SalesReportRow sumSales(List<SalesReportRow> rows) {
        long[] t = new long[6];
        for (SalesReportRow r : rows) {
            t[0] += r.completedSalesCount();
            t[1] += r.grossSalesAmount();
            t[2] += r.discountAmount();
            t[4] += r.paymentCollectedAmount();
            t[5] += r.outstandingAmount();
        }
        return salesRow(TOTAL_KEY, "Total", t);
    }

    private static String labelOf(SalesGroupBy by, String key, Map<UUID, String> locationNames) {
        return switch (by) {
            case NONE -> "All";
            case DAY -> key;
            case LOCATION -> {
                UUID id = parse(key);
                yield id == null ? key : locationNames.get(id);
            }
        };
    }

    // ---------------------------------------------------------------- stock

    @Override
    public StockReport getStockReport(ReportActor actor, UUID locationId, UUID productId, String status, int page,
                                      int size) {
        if (page < 0) {
            throw new InvalidReportRequestException("page cannot be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidReportRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return buildStockReport(actor, locationId, productId, status, page, size);
    }

    /** Same report without the API page-size cap; the export uses it to read up to its own row limit. */
    public StockReport buildStockReport(ReportActor actor, UUID locationId, UUID productId, String status, int page,
                                        int size) {
        StockStatus wanted = parseStatus(status);
        LocationScope scope = scope(actor, locationId);
        Instant asOf = Instant.now();
        if (scope.isEmpty()) {
            return new StockReport(asOf, scopeOf(scope), List.of(), page, size, 0, 0);
        }
        ReportStockPort.StockBalancePage result =
                source(() -> stock.balances(scope, productId, wanted, page, size));
        Map<String, Long> inTransit = new HashMap<>();
        for (ReportTransferPort.InTransit t : source(() -> transfers.inTransitByDestination(scope))) {
            inTransit.merge(t.destinationLocationId() + "|" + t.productId(), t.quantity(), Long::sum);
        }
        Set<UUID> locationIds = new HashSet<>();
        Set<UUID> productIds = new HashSet<>();
        for (ReportStockPort.StockBalance b : result.items()) {
            locationIds.add(b.locationId());
            productIds.add(b.productId());
        }
        Map<UUID, String> locationNames = source(() -> locations.locationNames(locationIds));
        Map<UUID, String> productNames = source(() -> names.productNames(productIds));
        List<StockReportRow> rows = new ArrayList<>();
        for (ReportStockPort.StockBalance b : result.items()) {
            rows.add(new StockReportRow(b.locationId(), locationNames.get(b.locationId()), b.productId(),
                    productNames.get(b.productId()), b.quantity(),
                    inTransit.getOrDefault(b.locationId() + "|" + b.productId(), 0L), b.lowStockThreshold(),
                    stockPolicy.statusOf(b.quantity(), b.lowStockThreshold()), null));
        }
        int pages = (int) ((result.total() + size - 1) / size);
        return new StockReport(asOf, scopeOf(scope), List.copyOf(rows), page, size, result.total(), pages);
    }

    private static StockStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return StockStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidReportRequestException("status must be OUT_OF_STOCK, LOW or OK");
        }
    }

    // ----------------------------------------------------------- purchasing

    @Override
    public PurchasingReport getPurchasingReport(ReportActor actor, LocalDate from, LocalDate to, UUID locationId,
                                                UUID supplierId) {
        ReportPeriod period = ReportPeriod.of(from, to);
        LocationScope scope = scope(actor, locationId);
        Instant asOf = Instant.now();
        List<ReportPurchasingPort.PurchasingTotal> totals = scope.isEmpty() ? List.of()
                : source(() -> purchasing.purchasingTotals(period, scope, supplierId));
        Map<UUID, String> supplierNames = source(() -> names.supplierNames(
                totals.stream().map(ReportPurchasingPort.PurchasingTotal::supplierId).filter(Objects::nonNull).toList()));
        List<PurchasingReportRow> rows = new ArrayList<>();
        long[] t = new long[7];
        for (ReportPurchasingPort.PurchasingTotal p : totals) {
            rows.add(new PurchasingReportRow(p.supplierId(), supplierNames.get(p.supplierId()), p.purchaseCount(),
                    p.orderedQuantity(), p.orderedAmount(), p.acceptedQuantity(), p.rejectedQuantity(),
                    p.purchaseCost(), p.discrepancyCount()));
            t[0] += p.purchaseCount();
            t[1] += p.orderedQuantity();
            t[2] += p.orderedAmount();
            t[3] += p.acceptedQuantity();
            t[4] += p.rejectedQuantity();
            t[5] += p.purchaseCost();
            t[6] += p.discrepancyCount();
        }
        rows.sort(Comparator.comparing(PurchasingReportRow::supplierName,
                Comparator.nullsLast(String::compareToIgnoreCase)));
        PurchasingReportRow total = new PurchasingReportRow(null, "Total", t[0], t[1], t[2], t[3], t[4], t[5], t[6]);
        return new PurchasingReport(from, to, asOf, scopeOf(scope), List.copyOf(rows), total);
    }

    // ------------------------------------------------------------ transfers

    @Override
    public TransferReport getTransferReport(ReportActor actor, LocalDate from, LocalDate to, UUID locationId,
                                            UUID sourceLocationId, UUID destinationLocationId) {
        ReportPeriod period = ReportPeriod.of(from, to);
        LocationScope scope = scope(actor, locationId);
        Instant asOf = Instant.now();
        List<ReportTransferPort.TransferTotal> totals = scope.isEmpty() ? List.of()
                : source(() -> transfers.transferTotals(period, scope, sourceLocationId, destinationLocationId));
        Set<UUID> ids = new HashSet<>();
        for (ReportTransferPort.TransferTotal x : totals) {
            ids.add(x.sourceLocationId());
            ids.add(x.destinationLocationId());
        }
        Map<UUID, String> locationNames = source(() -> locations.locationNames(ids));
        List<TransferReportRow> rows = new ArrayList<>();
        long[] t = new long[6];
        for (ReportTransferPort.TransferTotal x : totals) {
            rows.add(new TransferReportRow(x.sourceLocationId(), locationNames.get(x.sourceLocationId()),
                    x.destinationLocationId(), locationNames.get(x.destinationLocationId()),
                    x.sentQuantity(), x.receivedQuantity(), x.damagedQuantity(), x.missingQuantity(),
                    x.pendingTransferCount(), x.discrepancyCount()));
            t[0] += x.sentQuantity();
            t[1] += x.receivedQuantity();
            t[2] += x.damagedQuantity();
            t[3] += x.missingQuantity();
            t[4] += x.pendingTransferCount();
            t[5] += x.discrepancyCount();
        }
        rows.sort(Comparator.comparing(TransferReportRow::sourceLocationName,
                        Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(TransferReportRow::destinationLocationName,
                        Comparator.nullsLast(String::compareToIgnoreCase)));
        TransferReportRow total = new TransferReportRow(null, "Total", null, null, t[0], t[1], t[2], t[3], t[4], t[5]);
        return new TransferReport(from, to, asOf, scopeOf(scope), List.copyOf(rows), total);
    }

    // ---------------------------------------------------------------- debts

    @Override
    public DebtReport getCustomerDebtReport(ReportActor actor, LocalDate from, LocalDate to, UUID locationId,
                                            UUID customerId, boolean onlyOutstanding) {
        ReportPeriod period = ReportPeriod.of(from, to);
        LocationScope scope = scope(actor, locationId);
        Instant asOf = Instant.now();
        List<ReportPaymentPort.DebtLine> lines = scope.isEmpty() ? List.of()
                : source(() -> payments.debtRows(period, scope, customerId, onlyOutstanding));
        Map<UUID, String> customerNames = source(() -> names.customerNames(
                lines.stream().map(ReportPaymentPort.DebtLine::customerId).filter(Objects::nonNull).toList()));
        Map<UUID, String> locationNames = source(() -> locations.locationNames(
                lines.stream().map(ReportPaymentPort.DebtLine::locationId).filter(Objects::nonNull).toList()));
        List<DebtReportRow> rows = new ArrayList<>();
        long due = 0;
        long paid = 0;
        for (ReportPaymentPort.DebtLine l : lines) {
            rows.add(new DebtReportRow(l.customerId(), customerNames.get(l.customerId()), l.locationId(),
                    locationNames.get(l.locationId()), l.amountDue(), l.amountPaid(), l.amountDue() - l.amountPaid()));
            due += l.amountDue();
            paid += l.amountPaid();
        }
        rows.sort(Comparator.comparingLong(DebtReportRow::outstandingBalance).reversed()
                .thenComparing(DebtReportRow::customerName, Comparator.nullsLast(String::compareToIgnoreCase)));
        DebtReportRow total = new DebtReportRow(null, "Total", null, null, due, paid, due - paid);
        return new DebtReport(from, to, asOf, scopeOf(scope), onlyOutstanding, List.copyOf(rows), total);
    }

    // ----------------------------------------------------------- top products

    @Override
    public TopProductsReport getTopSellingProducts(ReportActor actor, LocalDate from, LocalDate to, UUID locationId,
                                                   TopMetric metric, Integer limit) {
        ReportPeriod period = ReportPeriod.of(from, to);
        TopMetric by = metric == null ? TopMetric.QUANTITY : metric;
        int n = limit == null ? DEFAULT_TOP_LIMIT : limit;
        if (n < 1 || n > MAX_TOP_LIMIT) {
            throw new InvalidReportRequestException("limit must be between 1 and " + MAX_TOP_LIMIT);
        }
        LocationScope scope = scope(actor, locationId);
        Instant asOf = Instant.now();
        List<ReportSalesPort.TopProduct> top = scope.isEmpty() ? List.of()
                : source(() -> sales.topProducts(period, scope, by, n));
        Map<UUID, String> productNames = source(() -> names.productNames(
                top.stream().map(ReportSalesPort.TopProduct::productId).toList()));
        Comparator<ReportSalesPort.TopProduct> order = by == TopMetric.VALUE
                ? Comparator.comparingLong(ReportSalesPort.TopProduct::salesValue).reversed()
                : Comparator.comparingLong(ReportSalesPort.TopProduct::quantitySold).reversed();
        List<ReportSalesPort.TopProduct> sorted = new ArrayList<>(top);
        sorted.sort(order.thenComparing(p -> productNames.getOrDefault(p.productId(), "￿"),
                String::compareToIgnoreCase).thenComparing(ReportSalesPort.TopProduct::productId));
        List<TopProductsReportRow> rows = new ArrayList<>();
        for (ReportSalesPort.TopProduct p : sorted.subList(0, Math.min(n, sorted.size()))) {
            rows.add(new TopProductsReportRow(rows.size() + 1, p.productId(), productNames.get(p.productId()),
                    p.quantitySold(), p.salesValue()));
        }
        return new TopProductsReport(from, to, asOf, scopeOf(scope), by, n, List.copyOf(rows));
    }

    // -------------------------------------------------------------- helpers

    private LocationScope scope(ReportActor actor, UUID requestedLocationId) {
        Set<UUID> assigned = actor.canViewAll() ? Set.of()
                : source(() -> locations.assignedLocationIds(actor.userId()));
        return scopePolicy.resolve(actor.canViewAll(), assigned, requestedLocationId);
    }

    private static ReportScope scopeOf(LocationScope scope) {
        return ReportScope.from(scope.all(), scope.locationIds());
    }

    private static <T> T source(Supplier<T> call) {
        try {
            return call.get();
        } catch (ReportsDomainException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ReportSourceUnavailableException("A report source is unavailable, try again later");
        }
    }

    private static Set<UUID> uuidKeys(Collection<String> keys) {
        Set<UUID> ids = new HashSet<>();
        for (String k : keys) {
            UUID id = parse(k);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static UUID parse(String key) {
        try {
            return UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}