package tsgp.yah_enterprise.dashboard.presentation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.dashboard.application.dto.DashboardActor;
import tsgp.yah_enterprise.dashboard.application.port.in.*;
import tsgp.yah_enterprise.dashboard.domain.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Read-only. Every response is Cache-Control: no-store. The scope comes from the caller's permissions. */
@RestController
@RequestMapping("/yah/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Current overview and alerts")
public class DashboardController {

    private static final String CAN_VIEW = "hasAuthority('" + PermissionCatalog.DASHBOARD_VIEW + "')";
    private static final String USER_ID = "userId()";
    private static final String VIEW_ALL = "hasPermission('" + PermissionCatalog.DASHBOARD_VIEW_ALL + "')";

    private final GetDashboardSummaryUseCase summaryUseCase;
    private final GetSalesByLocationUseCase salesByLocationUseCase;
    private final GetLowStockAlertsUseCase lowStockUseCase;
    private final GetPendingOrdersUseCase pendingOrdersUseCase;
    private final GetPendingTransfersUseCase pendingTransfersUseCase;
    private final GetRecentActivityUseCase recentActivityUseCase;
    private final GetOperationalAlertsUseCase alertsUseCase;

    private static <T> ResponseEntity<ApiResponse<T>> ok(String message, T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(message, body));
    }

    @GetMapping("/summary")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Main metrics of today for the caller's scope (all locations needs DASHBOARD_VIEW_ALL)")
    public ResponseEntity<ApiResponse<DashboardSummary>> summary(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam(required = false) UUID locationId) {
        return ok("Dashboard summary",
                summaryUseCase.getDashboardSummary(new DashboardActor(userId, viewAll), locationId));
    }

    @GetMapping("/sales-by-location")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Sales and payments per location (default period: today)")
    public ResponseEntity<ApiResponse<List<LocationSalesSummary>>> salesByLocation(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ok("Sales by location",
                salesByLocationUseCase.getSalesByLocation(new DashboardActor(userId, viewAll), from, to));
    }

    @GetMapping("/low-stock")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Out-of-stock and low-stock alerts (limit 1 to 100, default 20)")
    public ResponseEntity<ApiResponse<List<OperationalAlert>>> lowStock(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) Integer limit) {
        return ok("Low stock alerts",
                lowStockUseCase.getLowStockAlerts(new DashboardActor(userId, viewAll), locationId, limit));
    }

    @GetMapping("/pending-orders")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Orders waiting for validation, oldest first")
    public ResponseEntity<ApiResponse<OrderPendingPage>> pendingOrders(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ok("Pending orders", pendingOrdersUseCase.getPendingOrders(new DashboardActor(userId, viewAll),
                locationId, page, size));
    }

    @GetMapping("/pending-transfers")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Transfers awaiting dispatch or receipt, oldest first")
    public ResponseEntity<ApiResponse<TransferPendingPage>> pendingTransfers(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ok("Pending transfers", pendingTransfersUseCase.getPendingTransfers(
                new DashboardActor(userId, viewAll), locationId, page, size));
    }

    @GetMapping("/recent-activity")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Latest business actions in scope (limit up to 50). Security entries are not shown")
    public ResponseEntity<ApiResponse<List<RecentActivityItem>>> recentActivity(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam(required = false) Integer limit) {
        return ok("Recent activity",
                recentActivityUseCase.getRecentActivity(new DashboardActor(userId, viewAll), limit));
    }

    @GetMapping("/alerts")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Current operational alerts, most severe first (at most 100, with totalCount)")
    public ResponseEntity<ApiResponse<OperationalAlertsResult>> alerts(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = VIEW_ALL) boolean viewAll,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) AlertSeverity severity) {
        return ok("Operational alerts",
                alertsUseCase.getOperationalAlerts(new DashboardActor(userId, viewAll), locationId, severity));
    }
}
