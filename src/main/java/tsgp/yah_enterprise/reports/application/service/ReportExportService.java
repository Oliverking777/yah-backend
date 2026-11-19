package tsgp.yah_enterprise.reports.application.service;

import tsgp.yah_enterprise.reports.application.dto.ExportedReport;
import tsgp.yah_enterprise.reports.application.dto.ReportActor;
import tsgp.yah_enterprise.reports.application.dto.ReportExportRequest;
import tsgp.yah_enterprise.reports.application.port.in.ExportReportUseCase;
import tsgp.yah_enterprise.reports.domain.exception.InvalidReportRequestException;
import tsgp.yah_enterprise.reports.domain.exception.ReportExportTooLargeException;
import tsgp.yah_enterprise.reports.domain.model.*;
import tsgp.yah_enterprise.reports.domain.port.ReportAuditPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** CSV export of the six reports. At most 10,000 rows. Each export is audited as REPORT.EXPORTED. */
@Service
@Transactional
public class ReportExportService implements ExportReportUseCase {

    public static final int MAX_ROWS = 10_000;

    private final ReportService reports;
    private final ReportAuditPort audit;

    @Autowired
    public ReportExportService(ReportService reports, ReportAuditPort audit) {
        this.reports = reports;
        this.audit = audit;
    }

    @Override
    public ExportedReport exportReport(ReportActor actor, ReportExportRequest q) {
        if (q == null || q.report() == null) {
            throw new InvalidReportRequestException("A report name is required");
        }
        List<List<String>> table = new ArrayList<>();
        String file;
        switch (q.report()) {
            case SALES -> {
                SalesReport r = reports.getSalesReport(actor, q.from(), q.to(), q.locationId(), q.groupBy());
                table.add(List.of("group", "completedSalesCount", "grossSalesAmount", "discountAmount",
                        "netSalesAmount", "paymentCollectedAmount", "outstandingAmount"));
                for (SalesReportRow x : r.rows()) {
                    table.add(List.of(text(x.groupLabel() == null ? x.groupKey() : x.groupLabel()),
                            n(x.completedSalesCount()), n(x.grossSalesAmount()), n(x.discountAmount()),
                            n(x.netSalesAmount()), n(x.paymentCollectedAmount()), n(x.outstandingAmount())));
                }
                file = name(q);
            }
            case STOCK -> {
                StockReport r = reports.buildStockReport(actor, q.locationId(), q.productId(), q.status(), 0,
                        MAX_ROWS + 1);
                table.add(List.of("location", "product", "quantityOnHand", "quantityInTransit",
                        "lowStockThreshold", "stockStatus"));
                for (StockReportRow x : r.rows()) {
                    table.add(List.of(text(x.locationName()), text(x.productName()), n(x.quantityOnHand()),
                            n(x.quantityInTransit()),
                            x.lowStockThreshold() == null ? "" : n(x.lowStockThreshold()),
                            x.stockStatus().name()));
                }
                file = "stock-" + LocalDate.now(ZoneId.of("Africa/Douala")) + ".csv";
            }
            case PURCHASING -> {
                PurchasingReport r = reports.getPurchasingReport(actor, q.from(), q.to(), q.locationId(),
                        q.supplierId());
                table.add(List.of("supplier", "purchaseCount", "orderedQuantity", "orderedAmount",
                        "acceptedQuantity", "rejectedQuantity", "purchaseCost", "discrepancyCount"));
                for (PurchasingReportRow x : r.rows()) {
                    table.add(List.of(text(x.supplierName()), n(x.purchaseCount()), n(x.orderedQuantity()),
                            n(x.orderedAmount()), n(x.acceptedQuantity()), n(x.rejectedQuantity()),
                            n(x.purchaseCost()), n(x.discrepancyCount())));
                }
                file = name(q);
            }
            case TRANSFERS -> {
                TransferReport r = reports.getTransferReport(actor, q.from(), q.to(), q.locationId(),
                        q.sourceLocationId(), q.destinationLocationId());
                table.add(List.of("source", "destination", "sentQuantity", "receivedQuantity",
                        "damagedQuantity", "missingQuantity", "pendingTransferCount", "discrepancyCount"));
                for (TransferReportRow x : r.rows()) {
                    table.add(List.of(text(x.sourceLocationName()), text(x.destinationLocationName()),
                            n(x.sentQuantity()), n(x.receivedQuantity()), n(x.damagedQuantity()),
                            n(x.missingQuantity()), n(x.pendingTransferCount()), n(x.discrepancyCount())));
                }
                file = name(q);
            }
            case DEBTS -> {
                DebtReport r = reports.getCustomerDebtReport(actor, q.from(), q.to(), q.locationId(),
                        q.customerId(), q.onlyOutstanding() == null || q.onlyOutstanding());
                table.add(List.of("customer", "location", "amountDue", "amountPaid", "outstandingBalance"));
                for (DebtReportRow x : r.rows()) {
                    table.add(List.of(text(x.customerName()), text(x.locationName()), n(x.amountDue()),
                            n(x.amountPaid()), n(x.outstandingBalance())));
                }
                file = name(q);
            }
            case TOP_PRODUCTS -> {
                TopProductsReport r = reports.getTopSellingProducts(actor, q.from(), q.to(), q.locationId(),
                        q.metric(), q.limit());
                table.add(List.of("rank", "product", "quantitySold", "salesValue"));
                for (TopProductsReportRow x : r.rows()) {
                    table.add(List.of(n(x.rank()), text(x.productName()), n(x.quantitySold()),
                            n(x.salesValue())));
                }
                file = name(q);
            }
            default -> throw new InvalidReportRequestException("Unknown report");
        }
        int rows = table.size() - 1;
        if (rows > MAX_ROWS) {
            throw new ReportExportTooLargeException("The export has more than " + MAX_ROWS
                    + " rows, narrow the period or the location");
        }
        audit.record(new ReportAuditPort.AuditRecord(actor.userId(), "REPORT.EXPORTED", "REPORT", null,
                q.locationId(), null, filters(q, rows)));
        return new ExportedReport(file, toCsv(table).getBytes(StandardCharsets.UTF_8), rows);
    }

    private static List<ReportAuditPort.Change> filters(ReportExportRequest q, int rows) {
        List<ReportAuditPort.Change> c = new ArrayList<>();
        add(c, "report", q.report().path());
        add(c, "format", "csv");
        add(c, "from", q.from());
        add(c, "to", q.to());
        add(c, "locationId", q.locationId());
        add(c, "groupBy", q.groupBy());
        add(c, "productId", q.productId());
        add(c, "status", q.status());
        add(c, "supplierId", q.supplierId());
        add(c, "customerId", q.customerId());
        add(c, "sourceLocationId", q.sourceLocationId());
        add(c, "destinationLocationId", q.destinationLocationId());
        add(c, "metric", q.metric());
        add(c, "rowCount", rows);
        return c;
    }

    private static void add(List<ReportAuditPort.Change> list, String field, Object value) {
        if (value != null) {
            list.add(new ReportAuditPort.Change(field, null, String.valueOf(value)));
        }
    }

    private static String name(ReportExportRequest q) {
        return q.report().path() + "-" + q.from() + "-to-" + q.to() + ".csv";
    }

    private static String n(long v) {
        return Long.toString(v);
    }

    /** Text cells starting with = + - @ tab or CR would run as formulas in a spreadsheet: prefix an apostrophe. */
    static String text(String v) {
        if (v == null) {
            return "";
        }
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
            return "'" + v;
        }
        return v;
    }

    static String toCsv(List<List<String>> table) {
        StringBuilder sb = new StringBuilder();
        for (List<String> row : table) {
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                String cell = row.get(i);
                if (cell.contains(",") || cell.contains("\"") || cell.contains("\n") || cell.contains("\r")) {
                    sb.append('"').append(cell.replace("\"", "\"\"")).append('"');
                } else {
                    sb.append(cell);
                }
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }
}