package tsgp.yah_enterprise.sales.presentation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.sales.application.dto.*;
import tsgp.yah_enterprise.sales.application.port.in.*;
import tsgp.yah_enterprise.sales.domain.model.OrderStatus;
import tsgp.yah_enterprise.sales.domain.repository.SummaryGroupBy;
import tsgp.yah_enterprise.sales.presentation.dto.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * From an employee creating a PENDING order to a Manager completing it. An order is not a
 * sale until it is COMPLETED. There is no delete: cancelled orders stay in the history.
 * /summary is a literal path and is never read as an orderId.
 */
@RestController
@RequestMapping("/yah/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders and sales", description = "Create, complete and cancel orders; receipts and summaries")
public class OrderController {

    private static final String CAN_CREATE = "hasAuthority('" + PermissionCatalog.SALE_CREATE + "')";
    private static final String CAN_VALIDATE = "hasAuthority('" + PermissionCatalog.SALE_VALIDATE + "')";
    private static final String CAN_COMPLETE = "hasAuthority('" + PermissionCatalog.SALE_COMPLETE + "')";
    private static final String CAN_READ = "hasAuthority('" + PermissionCatalog.SALE_READ + "')";
    private static final String CAN_CANCEL = "hasAnyAuthority('" + PermissionCatalog.SALE_CREATE + "', '"
            + PermissionCatalog.SALE_VALIDATE + "')";
    private static final String CAN_REPORT = "hasAuthority('" + PermissionCatalog.REPORT_SALES_READ + "')";

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final CompleteSaleUseCase completeSaleUseCase;
    private final GetSaleReceiptUseCase getSaleReceiptUseCase;
    private final GetSalesSummaryUseCase getSalesSummaryUseCase;
    private final GetOrderHistoryUseCase getOrderHistoryUseCase;

    @PostMapping
    @PreAuthorize(CAN_CREATE)
    @Operation(summary = "Create an order (PENDING)",
            description = "You must work at the location. Every line is priced by the server. Sending the same orderId again returns the existing order.")
    public ResponseEntity<ApiResponse<OrderResult>> createOrder(@Valid @RequestBody CreateOrderRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.ANY_LOCATION) boolean anyLocation) {
        OrderResult created = createOrderUseCase.createOrder(request.toCommand(new OrderActor(userId, anyLocation)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orderId}").buildAndExpand(created.orderId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Order created", created));
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    @Operation(summary = "List orders",
            description = "Newest first. Without a role that works across every location you only see your own location. "
                    + "from is inclusive, to is exclusive (ISO-8601, for example 2026-10-01T00:00:00Z).")
    public ApiResponse<OrderPageResult> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID createdBy,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(listOrdersUseCase.listOrders(new OrderPageRequest(page, size, locationId, status,
                customerId, createdBy, from, to, new OrderActor(userId, anyLocation))));
    }

    @GetMapping("/summary")
    @PreAuthorize(CAN_REPORT)
    @Operation(summary = "Sales summary",
            description = "Count and total of COMPLETED orders per employee, location or day. from is inclusive, to is exclusive.")
    public ApiResponse<SalesSummaryResult> summary(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "DAY") SummaryGroupBy groupBy,
            @RequestParam(required = false) UUID locationId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(getSalesSummaryUseCase.getSalesSummary(new SalesSummaryRequest(from, to, groupBy,
                locationId, new OrderActor(userId, anyLocation))));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize(CAN_READ)
    @Operation(summary = "Get an order", description = "Lines with prices, discounts and totals.")
    public ApiResponse<OrderResult> getOrder(@PathVariable UUID orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(getOrderUseCase.getOrder(orderId, new OrderActor(userId, anyLocation)));
    }

    @PutMapping("/{orderId}")
    @PreAuthorize(CAN_CREATE)
    @Operation(summary = "Edit a draft", description = "Only a PENDING order, and only by its creator.")
    public ApiResponse<OrderResult> updateOrder(@PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(updateOrderUseCase.updateOrder(request.toCommand(orderId,
                new OrderActor(userId, anyLocation))));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize(CAN_CANCEL)
    @Operation(summary = "Cancel an order",
            description = "The creator cancels their own order; a holder of the validate permission cancels any pending order. Not after COMPLETED.")
    public ApiResponse<OrderResult> cancel(@PathVariable UUID orderId, @Valid @RequestBody ReasonRequest request,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.ANY_LOCATION) boolean anyLocation) {
        boolean canCancelAny = authentication.getAuthorities().stream()
                .anyMatch(a -> PermissionCatalog.SALE_VALIDATE.equals(a.getAuthority()));
        return ApiResponse.ok(cancelOrderUseCase.cancelOrder(new CancelOrderCommand(orderId, request.reason(),
                new OrderActor(userId, anyLocation), canCancelAny)));
    }

    @PostMapping("/{orderId}/complete")
    @PreAuthorize(CAN_COMPLETE)
    @Operation(summary = "Complete the sale",
            description = "Only a PENDING order. Prices again, applies discounts. In one transaction: stock is deducted, the payment is settled and the receipt is issued. "
                    + "Any failure rolls everything back and the order stays PENDING.")
    public ApiResponse<SaleReceiptResponse> complete(@PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CompleteSaleRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.ANY_LOCATION) boolean anyLocation) {
        CompleteSaleRequest body = request == null ? new CompleteSaleRequest(null, null, null) : request;
        return ApiResponse.ok(SaleReceiptResponse.from(completeSaleUseCase.completeSale(body.toCommand(orderId,
                new OrderActor(userId, anyLocation)))));
    }

    @GetMapping("/{orderId}/receipt")
    @PreAuthorize(CAN_READ)
    @Operation(summary = "Sale receipt", description = "Completed orders only (409 otherwise). Data only.")
    public ApiResponse<ReceiptResult> receipt(@PathVariable UUID orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(getSaleReceiptUseCase.getSaleReceipt(orderId, new OrderActor(userId, anyLocation)));
    }

    @GetMapping("/{orderId}/history")
    @PreAuthorize(CAN_READ)
    @Operation(summary = "Order history", description = "Every step, oldest first.")
    public ApiResponse<List<OrderEventResult>> history(@PathVariable UUID orderId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(getOrderHistoryUseCase.getOrderHistory(orderId, new OrderActor(userId, anyLocation)));
    }
}
