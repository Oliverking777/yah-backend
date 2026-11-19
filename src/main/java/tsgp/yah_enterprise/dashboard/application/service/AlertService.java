package tsgp.yah_enterprise.dashboard.application.service;

import tsgp.yah_enterprise.dashboard.application.dto.DashboardActor;
import tsgp.yah_enterprise.dashboard.application.port.in.GetLowStockAlertsUseCase;
import tsgp.yah_enterprise.dashboard.application.port.in.GetOperationalAlertsUseCase;
import tsgp.yah_enterprise.dashboard.domain.exception.InvalidDashboardRequestException;
import tsgp.yah_enterprise.dashboard.domain.model.*;
import tsgp.yah_enterprise.dashboard.domain.port.*;
import tsgp.yah_enterprise.dashboard.domain.service.AlertPolicy;
import tsgp.yah_enterprise.dashboard.domain.valueobject.AlertThresholds;
import tsgp.yah_enterprise.dashboard.domain.valueobject.DashboardPeriod;
import tsgp.yah_enterprise.dashboard.domain.valueobject.LocationScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Alerts derived from current business records. Nothing is stored: only conditions that hold now exist. A source
 * that fails is named in unavailableSources and the other alerts are still returned.
 */
@Service
public class AlertService implements GetLowStockAlertsUseCase, GetOperationalAlertsUseCase {

    public static final int DEFAULT_LOW_STOCK_LIMIT = 20;
    public static final int MAX_LOW_STOCK_LIMIT = 100;
    static final int SOURCE_LIMIT = 200;

    private final DashboardSalesPort sales;
    private final DashboardPaymentPort payments;
    private final DashboardStockPort stock;
    private final DashboardTransferPort transfers;
    private final DashboardPurchasingPort purchasing;
    private final DashboardNameDirectoryPort productDirectory;
    private final DashboardSupport support;
    private final AlertThresholds thresholds;
    private final AlertPolicy policy;
    private final Clock clock;

    @Autowired
    public AlertService(DashboardSalesPort sales, DashboardPaymentPort payments, DashboardStockPort stock,
                        DashboardTransferPort transfers, DashboardPurchasingPort purchasing,
                        DashboardLocationScopePort locations, DashboardNameDirectoryPort productDirectory,
                        SectionExecutor executor, AlertThresholds thresholds) {
        this(sales, payments, stock, transfers, purchasing, locations, productDirectory, executor, thresholds,
                Clock.systemUTC());
    }

    public AlertService(DashboardSalesPort sales, DashboardPaymentPort payments, DashboardStockPort stock,
                        DashboardTransferPort transfers, DashboardPurchasingPort purchasing,
                        DashboardLocationScopePort locations, DashboardNameDirectoryPort productDirectory,
                        SectionExecutor executor, AlertThresholds thresholds, Clock clock) {
        this.sales = sales;
        this.payments = payments;
        this.stock = stock;
        this.transfers = transfers;
        this.purchasing = purchasing;
        this.productDirectory = productDirectory;
        this.support = new DashboardSupport(locations, executor);
        this.thresholds = thresholds;
        this.policy = new AlertPolicy(thresholds);
        this.clock = clock;
    }

    /** A record that may become an alert, before names are known. */
    private record Candidate(AlertType type, String entityType, UUID entityId, String keySuffix, UUID locationId,
                             String label, UUID productId, Instant since) { }

    // ------------------------------------------------------------ low stock

    @Override
    public List<OperationalAlert> getLowStockAlerts(DashboardActor actor, UUID locationId, Integer limit) {
        int n = limit == null ? DEFAULT_LOW_STOCK_LIMIT : limit;
        if (n < 1 || n > MAX_LOW_STOCK_LIMIT) {
            throw new InvalidDashboardRequestException("limit must be between 1 and " + MAX_LOW_STOCK_LIMIT);
        }
        LocationScope scope = support.scope(actor, locationId);
        if (scope.isEmpty()) {
            return List.of();
        }
        Instant now = clock.instant();
        List<Candidate> found = DashboardSupport.required(() -> stockCandidates(scope, n, now));
        return build(found, now);
    }

    private List<Candidate> stockCandidates(LocationScope scope, int limit, Instant now) {
        List<Candidate> out = new ArrayList<>();
        for (DashboardStockPort.StockAlert s : stock.lowAndOutOfStock(scope, limit)) {
            AlertType type = s.quantity() <= 0 ? AlertType.OUT_OF_STOCK : AlertType.LOW_STOCK;
            out.add(new Candidate(type, "PRODUCT", s.productId(), ":" + s.locationId(), s.locationId(), null,
                    s.productId(), now));
        }
        return out;
    }

    // ------------------------------------------------------------ all alerts

    @Override
    public OperationalAlertsResult getOperationalAlerts(DashboardActor actor, UUID locationId,
                                                        AlertSeverity severity) {
        LocationScope scope = support.scope(actor, locationId);
        Instant now = clock.instant();
        long deadline = support.deadline();
        Map<String, Future<List<Candidate>>> sources = new LinkedHashMap<>();
        if (!scope.isEmpty()) {
            sources.put("stock", support.submit(() -> stockCandidates(scope, SOURCE_LIMIT, now)));
            sources.put("orders", support.submit(() -> sales.pendingOrders(scope, 0, SOURCE_LIMIT).items().stream()
                    .map(o -> new Candidate(AlertType.ORDER_AWAITING_VALIDATION, "ORDER", o.orderId(), "",
                            o.locationId(), o.orderNumber(), null, o.createdAt())).toList()));
            sources.put("credit", support.submit(() -> payments.pendingCredits(scope, SOURCE_LIMIT).stream()
                    .map(c -> new Candidate(AlertType.CREDIT_REQUEST_PENDING, "CREDIT_APPROVAL",
                            c.creditApprovalId(), "", c.locationId(), String.valueOf(c.saleId()), null, c.since()))
                    .toList()));
            sources.put("adjustments", support.submit(() -> stock.pendingAdjustments(scope, SOURCE_LIMIT).stream()
                    .map(a -> new Candidate(AlertType.STOCK_ADJUSTMENT_PENDING, "STOCK_ADJUSTMENT",
                            a.adjustmentId(), "", a.locationId(), a.adjustmentNumber(), null, a.since())).toList()));
            sources.put("transfers", support.submit(() -> transferCandidates(scope, now)));
            sources.put("purchases", support.submit(() -> purchaseCandidates(scope, now)));
        }
        List<String> unavailable = new ArrayList<>();
        List<Candidate> all = new ArrayList<>();
        for (Map.Entry<String, Future<List<Candidate>>> e : sources.entrySet()) {
            List<Candidate> got = support.await(e.getValue(), deadline);
            if (got == null) {
                unavailable.add(e.getKey());
            } else {
                all.addAll(got);
            }
        }
        List<OperationalAlert> alerts = new ArrayList<>(build(all, now));
        if (severity != null) {
            alerts.removeIf(a -> a.severity() != severity);
        }
        alerts.sort(Comparator.comparingInt((OperationalAlert a) -> a.severity().rank()).reversed()
                .thenComparing(OperationalAlert::createdAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(OperationalAlert::alertKey));
        int total = alerts.size();
        List<OperationalAlert> capped = alerts.subList(0, Math.min(total, OperationalAlertsResult.MAX_ALERTS));
        return new OperationalAlertsResult(now, support.scopeInfo(scope), List.copyOf(capped), total,
                List.copyOf(unavailable));
    }

    private List<Candidate> transferCandidates(LocationScope scope, Instant now) {
        List<Candidate> out = new ArrayList<>();
        // every pending transfer has been sent: the destination is the one that has to act
        for (DashboardTransferPort.PendingTransfer t : transfers.pendingTransfers(scope, 0, SOURCE_LIMIT).items()) {
            out.add(new Candidate(AlertType.TRANSFER_NOT_RECEIVED, "TRANSFER", t.transferId(), "",
                    t.destinationLocationId(), t.transferNumber(), null, t.sentAt()));
        }
        Instant since = now.minus(thresholds.discrepancyTransferWindow());
        for (DashboardTransferPort.DiscrepancyTransfer d : transfers.discrepancyTransfers(scope, since, SOURCE_LIMIT)) {
            out.add(new Candidate(AlertType.TRANSFER_DISCREPANCY, "TRANSFER", d.transferId(), "",
                    d.destinationLocationId(), d.transferNumber(), null, d.receivedAt()));
        }
        return out;
    }

    private List<Candidate> purchaseCandidates(LocationScope scope, Instant now) {
        List<Candidate> out = new ArrayList<>();
        // late from the day after the expected date, or a while after ordering when no date was given;
        // the destination is the one that has to receive the goods
        for (DashboardPurchasingPort.AwaitingDelivery a : purchasing.awaitingDelivery(scope, SOURCE_LIMIT)) {
            Instant due = a.expectedDeliveryDate() == null ? a.since().plus(thresholds.purchaseDeliveryDue())
                    : a.expectedDeliveryDate().plusDays(1).atStartOfDay(DashboardPeriod.BUSINESS_ZONE).toInstant();
            out.add(new Candidate(AlertType.PURCHASE_NOT_DELIVERED, "PURCHASE", a.purchaseId(), "",
                    a.locationId(), a.purchaseNumber(), null, due));
        }
        Instant since = now.minus(thresholds.discrepancyPurchaseWindow());
        for (DashboardPurchasingPort.DiscrepancyPurchase d : purchasing.discrepancyPurchases(scope, since,
                SOURCE_LIMIT)) {
            out.add(new Candidate(AlertType.PURCHASE_DISCREPANCY, "PURCHASE", d.purchaseId(), "",
                    d.locationId(), d.purchaseNumber(), null, d.confirmedAt()));
        }
        return out;
    }

    // ---------------------------------------------------------------- build

    private List<OperationalAlert> build(List<Candidate> candidates, Instant now) {
        Set<UUID> locationIds = new HashSet<>();
        Set<UUID> productIds = new HashSet<>();
        for (Candidate c : candidates) {
            if (c.locationId() != null) {
                locationIds.add(c.locationId());
            }
            if (c.productId() != null) {
                productIds.add(c.productId());
            }
        }
        Map<UUID, String> locationNames = support.names(locationIds);
        Map<UUID, String> productNames = productNames(productIds);
        List<OperationalAlert> out = new ArrayList<>();
        for (Candidate c : candidates) {
            Optional<AlertSeverity> severity = policy.severity(c.type(), c.since(), now);
            if (severity.isEmpty()) {
                continue;
            }
            String label = c.productId() != null ? productNames.getOrDefault(c.productId(), "A product") : c.label();
            String where = c.locationId() == null ? null : locationNames.get(c.locationId());
            out.add(new OperationalAlert(c.type().name() + ":" + c.entityType() + ":" + c.entityId() + c.keySuffix(),
                    c.type(), c.locationId(), c.entityType(), c.entityId(), severity.get(),
                    policy.message(c.type(), label, c.since(), now, where), c.since(), null));
        }
        return out;
    }

    private Map<UUID, String> productNames(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            return productDirectory.productNames(ids);
        } catch (RuntimeException e) {
            return Map.of();
        }
    }
}