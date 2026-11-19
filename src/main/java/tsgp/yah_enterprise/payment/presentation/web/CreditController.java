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

/** Selling on credit needs a Manager or Owner's approval, and the approver is never the requester. */
@RestController
@RequestMapping("/yah/api/v1/credits")
@RequiredArgsConstructor
@Tag(name = "Credit approvals", description = "Request, approve or reject credit for a sale")
public class CreditController {

    private static final String CAN_REQUEST = "hasAuthority('" + PermissionCatalog.PAYMENT_CREATE + "')";
    private static final String CAN_READ = "hasAuthority('" + PermissionCatalog.PAYMENT_READ + "')";
    private static final String CAN_DECIDE = "hasAuthority('" + PermissionCatalog.CREDIT_APPROVE + "')";

    private final RequestCreditUseCase requestCreditUseCase;
    private final DecideCreditUseCase decideCreditUseCase;
    private final GetCreditApprovalUseCase getCreditApprovalUseCase;
    private final ListCreditApprovalsUseCase listCreditApprovalsUseCase;

    @PostMapping
    @PreAuthorize(CAN_REQUEST)
    @Operation(summary = "Request credit for a sale",
            description = "Identified customers only. One open request per sale (409).")
    public ResponseEntity<ApiResponse<CreditApprovalResult>> request(
            @Valid @RequestBody RequestCreditRequest request, @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.ANY_LOCATION) boolean anyLocation) {
        CreditApprovalResult created = requestCreditUseCase.requestCredit(
                request.toCommand(new PaymentActor(userId, anyLocation)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.creditApprovalId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Credit requested", created));
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    @Operation(summary = "List credit approvals", description = "Newest first. Filter by status to see what waits for a decision.")
    public ApiResponse<PagedResult<CreditApprovalResult>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) CreditApprovalStatus status,
            @RequestParam(required = false) UUID customerId) {
        return ApiResponse.ok(listCreditApprovalsUseCase.listCreditApprovals(
                new CreditPageRequest(page, size, status, customerId)));
    }

    @GetMapping("/{creditApprovalId}")
    @PreAuthorize(CAN_READ)
    @Operation(summary = "Get a credit approval")
    public ApiResponse<CreditApprovalResult> get(@PathVariable UUID creditApprovalId) {
        return ApiResponse.ok(getCreditApprovalUseCase.getCreditApproval(creditApprovalId));
    }

    @PostMapping("/{creditApprovalId}/approve")
    @PreAuthorize(CAN_DECIDE)
    @Operation(summary = "Approve credit", description = "You cannot approve your own request (409). Decided once.")
    public ApiResponse<CreditApprovalResult> approve(@PathVariable UUID creditApprovalId,
            @Valid @RequestBody(required = false) ApproveCreditRequest request, @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.ANY_LOCATION) boolean anyLocation) {
        ApproveCreditRequest body = request == null ? new ApproveCreditRequest(null, null) : request;
        return ApiResponse.ok(decideCreditUseCase.approveCredit(new ApproveCreditCommand(creditApprovalId,
                body.approvedAmount(), body.note(), new PaymentActor(userId, anyLocation))));
    }

    @PostMapping("/{creditApprovalId}/reject")
    @PreAuthorize(CAN_DECIDE)
    @Operation(summary = "Reject credit", description = "A note is required. Decided once.")
    public ApiResponse<CreditApprovalResult> reject(@PathVariable UUID creditApprovalId,
            @Valid @RequestBody RejectCreditRequest request, @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = PaymentSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(decideCreditUseCase.rejectCredit(new RejectCreditCommand(creditApprovalId,
                request.note(), new PaymentActor(userId, anyLocation))));
    }
}
