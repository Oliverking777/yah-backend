package tsgp.yah_enterprise.reports.presentation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.reports.application.dto.*;
import tsgp.yah_enterprise.reports.application.port.in.*;
import tsgp.yah_enterprise.reports.domain.exception.InvalidReportRequestException;
import tsgp.yah_enterprise.reports.domain.model.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

/** Read-only reports. The location scope comes from the caller's permissions, never from the request. */
@RestController
@RequestMapping("/yah/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Sales, stock, purchasing, transfer and debt reports")
public class ReportController {

    private static final String CAN_VIEW = "hasAuthority('" + PermissionCatalog.REPORT_VIEW + "')";
    private static final String CAN_EXPORT = "hasAuthority('" + PermissionCatalog.REPORT_EXPORT + "')";
    private static final String USER_ID = "userId()";
    private static final String VIEW_ALL = "hasPermission('" + PermissionCatalog.REPORT_VIEW_ALL + "')";

    private final GetSalesReportUseCase salesUseCase;
    private final GetStockReportUseCase stockUseCase;
    private final GetPurchasingReportUseCase purchasingUseCase;
    private final GetTransferReportUseCase transferUseCase;
    private final GetCustomerDebtReportUseCase debtUseCase;
    private final GetTopSellingProductsReportUseCase topUseCase;
    private final ExportReportUseCase exportUseCase;

    @GetMapping("/sales")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Sales report: sales, cash collected and outstanding debt, by none, location or day")
    public ResponseEntity<ApiResponse<SalesReport>> sales(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) SalesGroupBy groupBy) {
        return ResponseEntity.ok(ApiResponse.ok("Sales report",
                salesUseCase.getSalesReport(new ReportActor(userId, viewAll), from, to, locationId, groupBy)));
    }

    @GetMapping("/stock")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Stock on hand and in transit, with status")
    public ResponseEntity<ApiResponse<StockReport>> stock(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Stock report", stockUseCase.getStockReport(
                new ReportActor(userId, viewAll), locationId, productId, status, page, size)));
    }

    @GetMapping("/purchasing")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Purchasing report per supplier")
    public ResponseEntity<ApiResponse<PurchasingReport>> purchasing(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) UUID supplierId) {
        return ResponseEntity.ok(ApiResponse.ok("Purchasing report", purchasingUseCase.getPurchasingReport(
                new ReportActor(userId, viewAll), from, to, locationId, supplierId)));
    }

    @GetMapping("/transfers")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Transfer report per route")
    public ResponseEntity<ApiResponse<TransferReport>> transfers(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) UUID sourceLocationId,
            @RequestParam(required = false) UUID destinationLocationId) {
        return ResponseEntity.ok(ApiResponse.ok("Transfer report", transferUseCase.getTransferReport(
                new ReportActor(userId, viewAll), from, to, locationId, sourceLocationId, destinationLocationId)));
    }

    @GetMapping("/debts")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Customer debt report")
    public ResponseEntity<ApiResponse<DebtReport>> debts(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(defaultValue = "true") boolean onlyOutstanding) {
        return ResponseEntity.ok(ApiResponse.ok("Debt report", debtUseCase.getCustomerDebtReport(
                new ReportActor(userId, viewAll), from, to, locationId, customerId, onlyOutstanding)));
    }

    @GetMapping("/top-products")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Top selling products by quantity or value (limit 1 to 50, default 10)")
    public ResponseEntity<ApiResponse<TopProductsReport>> topProducts(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) TopMetric metric,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(ApiResponse.ok("Top products", topUseCase.getTopSellingProducts(
                new ReportActor(userId, viewAll), from, to, locationId, metric, limit)));
    }

    @GetMapping(value = "/{reportName}/export", produces = "text/csv")
    @PreAuthorize(CAN_EXPORT)
    @Operation(summary = "Export a report as CSV (at most 10,000 rows). The export is audited")
    public ResponseEntity<byte[]> export(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @PathVariable String reportName,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) SalesGroupBy groupBy,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID sourceLocationId,
            @RequestParam(required = false) UUID destinationLocationId,
            @RequestParam(required = false) Boolean onlyOutstanding,
            @RequestParam(required = false) TopMetric metric,
            @RequestParam(required = false) Integer limit) {
        if (!"csv".equalsIgnoreCase(format)) {
            throw new InvalidReportRequestException("Only format=csv is supported");
        }
        ExportedReport csv = exportUseCase.exportReport(new ReportActor(userId, viewAll),
                new ReportExportRequest(ReportName.fromPath(reportName), from, to, locationId, groupBy, productId,
                        status, supplierId, customerId, sourceLocationId, destinationLocationId, onlyOutstanding,
                        metric, limit));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csv.filename() + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.content());
    }
}
