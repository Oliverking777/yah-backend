package tsgp.yah_enterprise.inventory.presentation.web;

import tsgp.yah_enterprise.inventory.application.dto.StockInitializationPageRequest;
import tsgp.yah_enterprise.inventory.application.port.in.GetStockInitializationUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.InitializeStockUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.ListStockInitializationsUseCase;
import tsgp.yah_enterprise.inventory.presentation.dto.InitializeStockRequest;
import tsgp.yah_enterprise.inventory.presentation.dto.StockInitializationPageResponse;
import tsgp.yah_enterprise.inventory.presentation.dto.StockInitializationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.iam.application.dto.AuthenticatedUser;

import java.net.URI;
import java.util.UUID;

/**
 * Stock initialization: the owner records the initial quantities of multiple products at one location.
 * Atomic — all products succeed or none do.
 */
@RestController
@RequestMapping("/yah/api/v1/stock/initializations")
@RequiredArgsConstructor
@Tag(name = "Stock Initialization",
        description = "Bulk initial stock setup: record the starting quantities of multiple products at one location")
public class StockInitializationController {

    private final InitializeStockUseCase initializeStockUseCase;
    private final GetStockInitializationUseCase getStockInitializationUseCase;
    private final ListStockInitializationsUseCase listStockInitializationsUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_INIT_CREATE + "')")
    @Operation(summary = "Initialize stock for multiple products at one location",
            description = "The owner selects a location and records the initial quantity of each product in one "
                    + "operation. The system validates all data, creates stock balances, records stock movements "
                    + "and an audit entry — all atomically. If one item fails, none are committed. "
                    + "A product can be initialized at a given location only once.")
    public ResponseEntity<ApiResponse<StockInitializationResponse>> initializeStock(
            @Valid @RequestBody InitializeStockRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        StockInitializationResponse created = StockInitializationResponse.from(
                initializeStockUseCase.initializeStock(request.toCommand(user.userId(), user.requiresLocation())));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{initializationId}").buildAndExpand(created.initializationId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Stock initialized", created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_INIT_READ + "')")
    @Operation(summary = "List stock initializations",
            description = "Newest first. status is COMPLETED or CANCELLED. A user limited to one location only sees "
                    + "that location's initializations.")
    public ApiResponse<StockInitializationPageResponse> listInitializations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID locationId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(StockInitializationPageResponse.from(listStockInitializationsUseCase.listInitializations(
                new StockInitializationPageRequest(page, size, status, locationId,
                        user.userId(), user.requiresLocation()))));
    }

    @GetMapping("/{initializationId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.STOCK_INIT_READ + "')")
    @Operation(summary = "Get a stock initialization by id", description = "Returns the initialization and all its "
            + "product lines with product and unit names.")
    public ApiResponse<StockInitializationResponse> getInitialization(
            @PathVariable UUID initializationId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(StockInitializationResponse.from(getStockInitializationUseCase
                .getInitialization(initializationId, user.userId(), user.requiresLocation())));
    }
}
