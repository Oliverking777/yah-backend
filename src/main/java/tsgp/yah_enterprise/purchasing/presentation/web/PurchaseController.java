package tsgp.yah_enterprise.purchasing.presentation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.purchasing.application.dto.*;
import tsgp.yah_enterprise.purchasing.application.port.in.*;
import tsgp.yah_enterprise.purchasing.domain.model.PurchaseStatus;
import tsgp.yah_enterprise.purchasing.presentation.dto.*;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

/**
 * Purchases from suppliers: SUBMITTED when created, then CONFIRMED (delivery recorded, stock added) or CANCELLED.
 * There is no delete. Stock changes only on confirm and on a direct purchase.
 */
@RestController
@RequestMapping("/yah/api/v1/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases", description = "Order goods from a supplier and confirm what was delivered")
public class PurchaseController {

    private static final String CAN_CREATE = "hasAuthority('" + PermissionCatalog.PURCHASE_CREATE + "')";
    private static final String CAN_VIEW = "hasAuthority('" + PermissionCatalog.PURCHASE_READ + "')";
    private static final String CAN_CONFIRM = "hasAuthority('" + PermissionCatalog.RECEIPT_CONFIRM + "')";
    private static final String CAN_CANCEL = "hasAuthority('" + PermissionCatalog.PURCHASE_CANCEL + "')";
    /** A direct purchase creates and confirms in one go, so it needs both permissions. */
    private static final String CAN_DIRECT = CAN_CREATE + " and " + CAN_CONFIRM;

    private final CreatePurchaseUseCase createPurchaseUseCase;
    private final UpdatePurchaseUseCase updatePurchaseUseCase;
    private final AddPurchaseItemUseCase addPurchaseItemUseCase;
    private final ChangePurchaseItemUseCase changePurchaseItemUseCase;
    private final RemovePurchaseItemUseCase removePurchaseItemUseCase;
    private final ConfirmPurchaseUseCase confirmPurchaseUseCase;
    private final CancelPurchaseUseCase cancelPurchaseUseCase;
    private final GetPurchaseUseCase getPurchaseUseCase;
    private final ListPurchasesUseCase listPurchasesUseCase;
    private final RecordDirectPurchaseUseCase recordDirectPurchaseUseCase;

    @PostMapping
    @PreAuthorize(CAN_CREATE)
    @Operation(summary = "Create a purchase (SUBMITTED)",
            description = "You must work at the destination location. No stock changes until the delivery is confirmed. "
                    + "Sending the same purchaseId again returns the existing purchase.")
    public ResponseEntity<ApiResponse<PurchaseResult>> create(@Valid @RequestBody CreatePurchaseRequest request,
                                                              @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
                                                              @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        PurchaseResult created = createPurchaseUseCase.createPurchase(
                request.toCommand(new PurchaseActor(userId, anyLocation)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{purchaseId}").buildAndExpand(created.purchaseId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Purchase created", created));
    }

    @PostMapping("/direct")
    @PreAuthorize(CAN_DIRECT)
    @Operation(summary = "Record a direct purchase (bought and received on the spot)",
            description = "Creates and confirms the purchase in one transaction: the kept quantities go into stock at once. "
                    + "Sending the same purchaseId again returns what was already recorded.")
    public ResponseEntity<ApiResponse<PurchaseResult>> recordDirect(
            @Valid @RequestBody DirectPurchaseRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        PurchaseResult recorded = recordDirectPurchaseUseCase.recordDirectPurchase(
                request.toCommand(new PurchaseActor(userId, anyLocation)));
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/yah/api/v1/purchases/{purchaseId}").buildAndExpand(recorded.purchaseId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Direct purchase recorded", recorded));
    }

    @GetMapping
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "List purchases",
            description = "Newest first. Without a role that works across every location you only see your own location. "
                    + "status=SUBMITTED lists the purchases awaiting delivery; hasDiscrepancy=true the deliveries with "
                    + "rejected or missing goods. from is inclusive, to is exclusive (ISO-8601).")
    public ApiResponse<PurchasePageResult> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) PurchaseStatus status,
            @RequestParam(required = false) Boolean hasDiscrepancy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(listPurchasesUseCase.listPurchases(new PurchasePageRequest(page, size, supplierId,
                locationId, status, hasDiscrepancy, from, to, new PurchaseActor(userId, anyLocation))));
    }

    @GetMapping("/{purchaseId}")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Read a purchase",
            description = "Lines with ordered quantities and, once confirmed, accepted, rejected and missing quantities.")
    public ApiResponse<PurchaseResult> get(@PathVariable UUID purchaseId,
                                           @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
                                           @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(getPurchaseUseCase.getPurchase(purchaseId, new PurchaseActor(userId, anyLocation)));
    }

    @PutMapping("/{purchaseId}")
    @PreAuthorize(CAN_CREATE)
    @Operation(summary = "Change a SUBMITTED purchase", description = "A missing field is left unchanged.")
    public ApiResponse<PurchaseResult> update(@PathVariable UUID purchaseId,
                                              @Valid @RequestBody UpdatePurchaseRequest request,
                                              @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
                                              @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok("Purchase updated", updatePurchaseUseCase.updatePurchase(
                request.toCommand(purchaseId, new PurchaseActor(userId, anyLocation))));
    }

    @PostMapping("/{purchaseId}/items")
    @PreAuthorize(CAN_CREATE)
    @Operation(summary = "Add a line to a SUBMITTED purchase")
    public ResponseEntity<ApiResponse<PurchaseResult>> addItem(@PathVariable UUID purchaseId,
                                                               @Valid @RequestBody PurchaseLineRequest request,
                                                               @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
                                                               @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        PurchaseResult result = addPurchaseItemUseCase.addPurchaseItem(new AddPurchaseItemCommand(purchaseId,
                request.toCommand(), new PurchaseActor(userId, anyLocation)));
        return ResponseEntity.status(201).body(ApiResponse.created("Line added", result));
    }

    @PutMapping("/{purchaseId}/items/{purchaseItemId}")
    @PreAuthorize(CAN_CREATE)
    @Operation(summary = "Change the quantity and price of a line of a SUBMITTED purchase")
    public ApiResponse<PurchaseResult> changeItem(@PathVariable UUID purchaseId, @PathVariable UUID purchaseItemId,
                                                  @Valid @RequestBody ChangePurchaseItemRequest request,
                                                  @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
                                                  @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok("Line updated", changePurchaseItemUseCase.changePurchaseItem(
                new ChangePurchaseItemCommand(purchaseId, purchaseItemId, request.quantity(), request.unitPrice(),
                        new PurchaseActor(userId, anyLocation))));
    }

    @DeleteMapping("/{purchaseId}/items/{purchaseItemId}")
    @PreAuthorize(CAN_CREATE)
    @Operation(summary = "Remove a line from a SUBMITTED purchase", description = "The last line cannot be removed; cancel the purchase instead.")
    public ResponseEntity<Void> removeItem(@PathVariable UUID purchaseId, @PathVariable UUID purchaseItemId,
                                           @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
                                           @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        removePurchaseItemUseCase.removePurchaseItem(new RemovePurchaseItemCommand(purchaseId, purchaseItemId,
                new PurchaseActor(userId, anyLocation)));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{purchaseId}/confirm")
    @PreAuthorize(CAN_CONFIRM)
    @Operation(summary = "Confirm the delivery (SUBMITTED to CONFIRMED)",
            description = "One line per purchase line. The accepted quantities go into stock in the same transaction; "
                    + "rejected or missing goods flag the purchase with a discrepancy. You must work at the destination "
                    + "location. A second confirmation is a 409.")
    public ApiResponse<PurchaseResult> confirm(@PathVariable UUID purchaseId,
                                               @Valid @RequestBody ConfirmPurchaseRequest request,
                                               @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
                                               @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok("Delivery confirmed", confirmPurchaseUseCase.confirmPurchase(
                request.toCommand(purchaseId, new PurchaseActor(userId, anyLocation))));
    }

    @PostMapping("/{purchaseId}/cancel")
    @PreAuthorize(CAN_CANCEL)
    @Operation(summary = "Cancel a purchase", description = "SUBMITTED purchases only. Changes no stock.")
    public ApiResponse<PurchaseResult> cancel(@PathVariable UUID purchaseId,
                                              @Valid @RequestBody PurchaseReasonRequest request,
                                              @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.USER_ID) UUID userId,
                                              @Parameter(hidden = true) @AuthenticationPrincipal(expression = PurchasingSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok("Purchase cancelled", cancelPurchaseUseCase.cancelPurchase(
                new PurchaseReasonCommand(purchaseId, request.reason(), new PurchaseActor(userId, anyLocation))));
    }
}