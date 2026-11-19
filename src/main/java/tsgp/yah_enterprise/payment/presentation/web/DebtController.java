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

/** Debt is derived from what each sale owes less what has been paid to it. It cannot be edited. */
@RestController
@RequestMapping("/yah/api/v1")
@RequiredArgsConstructor
@Tag(name = "Debts and balances", description = "What customers owe")
public class DebtController {

    private static final String CAN_BALANCE = "hasAuthority('" + PermissionCatalog.BALANCE_READ + "')";
    private static final String CAN_READ = "hasAuthority('" + PermissionCatalog.PAYMENT_READ + "')";

    private final ListDebtsUseCase listDebtsUseCase;
    private final GetCustomerBalanceUseCase getCustomerBalanceUseCase;
    private final ListPaymentsUseCase listPaymentsUseCase;

    @GetMapping("/debts")
    @PreAuthorize(CAN_BALANCE)
    @Operation(summary = "Debts that are still unpaid", description = "Oldest first. Without a role that works across every location you only see your own location.")
    public ApiResponse<PagedResult<DebtResult>> listDebts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID locationId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(listDebtsUseCase.listDebts(new DebtPageRequest(page, size, customerId, locationId,
                new PaymentActor(userId, anyLocation))));
    }

    @GetMapping("/customers/{customerId}/balance")
    @PreAuthorize(CAN_BALANCE)
    @Operation(summary = "What a customer owes", description = "Total of the unpaid part of all their sales.")
    public ApiResponse<CustomerBalanceResult> balance(@PathVariable UUID customerId) {
        return ApiResponse.ok(getCustomerBalanceUseCase.getBalance(customerId));
    }

    @GetMapping("/customers/{customerId}/payments")
    @PreAuthorize(CAN_READ)
    @Operation(summary = "Payments of a customer", description = "Newest first.")
    public ApiResponse<PagedResult<PaymentResult>> customerPayments(@PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(listPaymentsUseCase.listPayments(new PaymentPageRequest(page, size, customerId, null,
                null, new PaymentActor(userId, anyLocation))));
    }
}
