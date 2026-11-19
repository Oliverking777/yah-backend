package tsgp.yah_enterprise.purchasing.presentation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.purchasing.application.dto.*;
import tsgp.yah_enterprise.purchasing.application.port.in.*;

import java.util.UUID;

/** Lives in Purchasing so the Supplier module stays free of Purchasing. */
@RestController
@RequestMapping("/yah/api/v1/suppliers/{supplierId}")
@RequiredArgsConstructor
@Tag(name = "Purchases", description = "Order goods from a supplier and confirm what was delivered")
public class SupplierPurchasesController {

    private static final String CAN_VIEW = "hasAuthority('" + PermissionCatalog.PURCHASE_READ + "')";

    private final ListPurchasesUseCase listPurchasesUseCase;
    private final ListSupplierProductsUseCase listSupplierProductsUseCase;

    @GetMapping("/purchases")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Purchases from a supplier", description = "Newest first.")
    public ApiResponse<PurchasePageResult> purchases(@PathVariable UUID supplierId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
                                                     @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(listPurchasesUseCase.listPurchases(new PurchasePageRequest(page, size, supplierId, null,
                null, null, null, null, new PurchaseActor(userId, anyLocation))));
    }

    @GetMapping("/products")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Products ordered from a supplier")
    public ApiResponse<SupplierProductPageResult> products(@PathVariable UUID supplierId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(listSupplierProductsUseCase.listSupplierProducts(
                new SupplierProductsRequest(supplierId, page, size)));
    }
}
