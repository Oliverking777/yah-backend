package tsgp.yah_enterprise.inventory.presentation.web;

import tsgp.yah_enterprise.inventory.application.dto.DecideStockAdjustmentCommand;
import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.StockLevelPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.StockMovementPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.UpdateStockThresholdCommand;
import tsgp.yah_enterprise.inventory.application.port.in.ApproveStockAdjustmentUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.GetProductStockUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.GetStockAdjustmentUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.ListStockAdjustmentsUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.ListStockLevelsUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.ListStockMovementsUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.RejectStockAdjustmentUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.RequestStockAdjustmentUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.UpdateStockThresholdUseCase;
import tsgp.yah_enterprise.inventory.presentation.dto.DecideStockAdjustmentRequest;
import tsgp.yah_enterprise.inventory.presentation.dto.ProductStockResponse;
import tsgp.yah_enterprise.inventory.presentation.dto.RequestStockAdjustmentRequest;
import tsgp.yah_enterprise.inventory.presentation.dto.StockAdjustmentPageResponse;
import tsgp.yah_enterprise.inventory.presentation.dto.StockAdjustmentResponse;
import tsgp.yah_enterprise.inventory.presentation.dto.StockLevelPageResponse;
import tsgp.yah_enterprise.inventory.presentation.dto.StockMovementPageResponse;
import tsgp.yah_enterprise.inventory.presentation.dto.UpdateStockThresholdRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.iam.application.dto.AuthenticatedUser;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

/**
 * Stock levels, the ledger, thresholds and adjustments.
 * There is no endpoint that writes stock directly: other modules use RecordStockMovementUseCase, and an
 * approved adjustment is the only HTTP path that changes a quantity.
 */
@RestController
@RequestMapping("/yah/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Stock levels per location, the movement ledger, low-stock thresholds and stock adjustments")
public class StockController {

    private final ListStockLevelsUseCase listStockLevelsUseCase;
    private final GetProductStockUseCase getProductStockUseCase;
    private final UpdateStockThresholdUseCase updateStockThresholdUseCase;
    private final ListStockMovementsUseCase listStockMovementsUseCase;
    private final RequestStockAdjustmentUseCase requestStockAdjustmentUseCase;
    private final ListStockAdjustmentsUseCase listStockAdjustmentsUseCase;
    private final GetStockAdjustmentUseCase getStockAdjustmentUseCase;
    private final ApproveStockAdjustmentUseCase approveStockAdjustmentUseCase;
    private final RejectStockAdjustmentUseCase rejectStockAdjustmentUseCase;

    // ---------------- stock levels ----------------

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_READ + "')")
    @Operation(summary = "List stock levels",
            description = "One row per product and location, in base units. A user limited to one location only sees that location.")
    public ApiResponse<StockLevelPageResponse> listStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) UUID productId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(StockLevelPageResponse.from(listStockLevelsUseCase.listStockLevels(
                new StockLevelPageRequest(page, size, locationId, productId, false,
                        user.userId(), user.requiresLocation()))));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_THRESHOLD_READ + "')")
    @Operation(summary = "List products at or below their low-stock threshold")
    public ApiResponse<StockLevelPageResponse> listLowStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID locationId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(StockLevelPageResponse.from(listStockLevelsUseCase.listStockLevels(
                new StockLevelPageRequest(page, size, locationId, null, true,
                        user.userId(), user.requiresLocation()))));
    }

    @GetMapping("/products/{productId}/locations/{locationId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_READ + "')")
    @Operation(summary = "Get the stock of one product at one location",
            description = "Returns 0 when nothing was ever recorded.")
    public ApiResponse<ProductStockResponse> getProductStock(
            @PathVariable UUID productId, @PathVariable UUID locationId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        // reuse the listing scope rules so a limited user cannot read another location
        listStockLevelsUseCase.listStockLevels(new StockLevelPageRequest(0, 1, locationId, productId, false,
                user.userId(), user.requiresLocation()));
        return ApiResponse.ok(ProductStockResponse.from(getProductStockUseCase.getProductStock(productId, locationId)));
    }

    @PutMapping("/products/{productId}/locations/{locationId}/threshold")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_THRESHOLD_UPDATE + "')")
    @Operation(summary = "Set or clear the low-stock threshold",
            description = "A null threshold removes it. The threshold never changes the quantity.")
    public ApiResponse<ProductStockResponse> updateThreshold(
            @PathVariable UUID productId, @PathVariable UUID locationId,
            @Valid @RequestBody UpdateStockThresholdRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok("Threshold updated", ProductStockResponse.from(
                updateStockThresholdUseCase.updateThreshold(new UpdateStockThresholdCommand(productId, locationId,
                        request.lowStockThreshold(), user.userId(), user.requiresLocation()))));
    }

    // ---------------- ledger ----------------

    @GetMapping("/movements")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_READ + "')")
    @Operation(summary = "List stock movements (the ledger)",
            description = "Newest first. Quantities are signed base units. from is inclusive, to is exclusive.")
    public ApiResponse<StockMovementPageResponse> listMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(StockMovementPageResponse.from(listStockMovementsUseCase.listStockMovements(
                new StockMovementPageRequest(page, size, productId, locationId, movementType, from, to,
                        user.userId(), user.requiresLocation()))));
    }

    // ---------------- adjustments ----------------

    @PostMapping("/adjustments")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_ADJUST_CREATE + "')")
    @Operation(summary = "Request a stock adjustment",
            description = "Creates a PENDING adjustment (damage, loss, supplier return, count correction, opening stock). "
                    + "No stock changes until a user with STOCK_ADJUST_APPROVE (the requester included) approves it.")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> requestAdjustment(
            @Valid @RequestBody RequestStockAdjustmentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        StockAdjustmentResponse created = StockAdjustmentResponse.from(requestStockAdjustmentUseCase
                .requestAdjustment(request.toCommand(user.userId(), user.requiresLocation())));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{adjustmentId}").buildAndExpand(created.adjustmentId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Stock adjustment requested", created));
    }

    @GetMapping("/adjustments")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_ADJUST_READ + "')")
    @Operation(summary = "List stock adjustments", description = "Newest first. status is PENDING, APPROVED or REJECTED.")
    public ApiResponse<StockAdjustmentPageResponse> listAdjustments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) UUID productId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(StockAdjustmentPageResponse.from(listStockAdjustmentsUseCase.listAdjustments(
                new StockAdjustmentPageRequest(page, size, status, locationId, productId,
                        user.userId(), user.requiresLocation()))));
    }

    @GetMapping("/adjustments/{adjustmentId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_ADJUST_READ + "')")
    @Operation(summary = "Get a stock adjustment by id")
    public ApiResponse<StockAdjustmentResponse> getAdjustment(
            @PathVariable UUID adjustmentId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(StockAdjustmentResponse.from(getStockAdjustmentUseCase
                .getAdjustment(adjustmentId, user.userId(), user.requiresLocation())));
    }

    @PostMapping("/adjustments/{adjustmentId}/approve")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_ADJUST_APPROVE + "')")
    @Operation(summary = "Approve a stock adjustment",
            description = "Moves the stock and records the before/after quantities. The requester may approve their own adjustment. "
                    + "A decrease larger than the stock returns 409 and the adjustment stays PENDING.")
    public ApiResponse<StockAdjustmentResponse> approveAdjustment(
            @PathVariable UUID adjustmentId,
            @RequestBody(required = false) @Valid DecideStockAdjustmentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok("Stock adjustment approved", StockAdjustmentResponse.from(
                approveStockAdjustmentUseCase.approve(new DecideStockAdjustmentCommand(adjustmentId, user.userId(),
                        request == null ? null : request.note(), user.requiresLocation()))));
    }

    @PostMapping("/adjustments/{adjustmentId}/reject")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_ADJUST_APPROVE + "')")
    @Operation(summary = "Reject a stock adjustment",
            description = "No stock changes. A reason of 5 to 500 characters is required.")
    public ApiResponse<StockAdjustmentResponse> rejectAdjustment(
            @PathVariable UUID adjustmentId,
            @RequestBody(required = false) @Valid DecideStockAdjustmentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok("Stock adjustment rejected", StockAdjustmentResponse.from(
                rejectStockAdjustmentUseCase.reject(new DecideStockAdjustmentCommand(adjustmentId, user.userId(),
                        request == null ? null : request.note(), user.requiresLocation()))));
    }
}