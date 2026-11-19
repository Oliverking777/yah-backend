package tsgp.yah_enterprise.payment.presentation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.payment.application.dto.*;
import tsgp.yah_enterprise.payment.application.port.in.*;
import tsgp.yah_enterprise.payment.domain.model.*;
import tsgp.yah_enterprise.payment.presentation.dto.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Payments are money received, not sales. One payment can pay several sales and one sale can have several
 * partial payments. There is no delete and no edit: a wrong payment is reversed with a reason.
 */
@RestController
@RequestMapping("/yah/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Record, allocate and reverse customer payments")
public class PaymentController {

    private static final String CAN_CREATE = "hasAuthority('" + PermissionCatalog.PAYMENT_CREATE + "')";
    private static final String CAN_READ = "hasAuthority('" + PermissionCatalog.PAYMENT_READ + "')";
    private static final String CAN_REVERSE = "hasAuthority('" + PermissionCatalog.CREDIT_APPROVE + "')";

    private final RecordPaymentUseCase recordPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final ListPaymentsUseCase listPaymentsUseCase;
    private final AllocatePaymentUseCase allocatePaymentUseCase;
    private final ReversePaymentUseCase reversePaymentUseCase;

    @PostMapping
    @PreAuthorize(CAN_CREATE)
    @Operation(summary = "Record a payment",
            description = "Mobile money needs the provider reference; the same reference is refused twice (409). "
                    + "Sending the same paymentId again returns the existing payment.")
    public ResponseEntity<ApiResponse<PaymentResult>> recordPayment(
            @Valid @RequestBody RecordPaymentRequest request, @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.ANY_LOCATION) boolean anyLocation) {
        PaymentResult created = recordPaymentUseCase.recordPayment(request.toCommand(new PaymentActor(userId, anyLocation)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{paymentId}").buildAndExpand(created.paymentId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Payment recorded", created));
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    @Operation(summary = "List payments",
            description = "Newest first. Without a role that works across every location you only see your own location.")
    public ApiResponse<PagedResult<PaymentResult>> listPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) PaymentStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(listPaymentsUseCase.listPayments(new PaymentPageRequest(page, size, customerId,
                locationId, status, new PaymentActor(userId, anyLocation))));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize(CAN_READ)
    @Operation(summary = "Get a payment")
    public ApiResponse<PaymentResult> getPayment(@PathVariable UUID paymentId, @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(getPaymentUseCase.getPayment(paymentId, new PaymentActor(userId, anyLocation)));
    }

    @PostMapping("/{paymentId}/allocations")
    @PreAuthorize(CAN_CREATE)
    @Operation(summary = "Allocate a payment to debts",
            description = "Give the sales and amounts, or send no list to pay the customer's oldest debts first. "
                    + "A payment cannot allocate more than it holds and a sale cannot receive more than it owes (409).")
    public ApiResponse<List<AllocationResult>> allocate(@PathVariable UUID paymentId,
            @Valid @RequestBody(required = false) AllocatePaymentRequest request, @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.ANY_LOCATION) boolean anyLocation) {
        List<AllocationLine> lines = request == null || request.allocations() == null ? null
                : request.allocations().stream().map(AllocationLineRequest::toLine).toList();
        return ApiResponse.ok(allocatePaymentUseCase.allocatePayment(
                new AllocatePaymentCommand(paymentId, lines, new PaymentActor(userId, anyLocation))));
    }

    @PostMapping("/{paymentId}/reverse")
    @PreAuthorize(CAN_REVERSE)
    @Operation(summary = "Reverse a payment",
            description = "Manager or Owner only, with a reason. The allocations are reversed and the debts come back. Never deleted.")
    public ApiResponse<PaymentResult> reverse(@PathVariable UUID paymentId,
            @Valid @RequestBody ReversePaymentRequest request, @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(reversePaymentUseCase.reversePayment(
                new ReversePaymentCommand(paymentId, request.reason(), new PaymentActor(userId, anyLocation))));
    }
}
