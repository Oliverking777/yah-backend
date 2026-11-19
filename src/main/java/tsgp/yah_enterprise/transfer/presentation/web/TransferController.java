package tsgp.yah_enterprise.transfer.presentation.web;

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
import tsgp.yah_enterprise.transfer.application.dto.CancelTransferCommand;
import tsgp.yah_enterprise.transfer.application.dto.TransferActor;
import tsgp.yah_enterprise.transfer.application.dto.TransferPageRequest;
import tsgp.yah_enterprise.transfer.application.port.in.*;
import tsgp.yah_enterprise.transfer.domain.model.TransferStatus;
import tsgp.yah_enterprise.transfer.presentation.dto.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Moves products between the warehouse and a store, or between two stores. Creating a transfer sends it
 * (SUBMITTED, in transit); the destination then confirms what arrived (RECEIVED), or either side calls it off
 * (CANCELLED, the goods go back to the source). There is no delete and no endpoint that writes stock directly.
 * /in-transit is a literal path and is never read as a transferId.
 */
@RestController
@RequestMapping("/yah/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Move products between locations: send, receive, cancel")
public class TransferController {

    private static final String CAN_CREATE = "hasAuthority('" + PermissionCatalog.TRANSFER_CREATE + "')";
    private static final String CAN_CONFIRM = "hasAuthority('" + PermissionCatalog.TRANSFER_CONFIRM + "')";
    private static final String CAN_READ = "hasAuthority('" + PermissionCatalog.TRANSFER_READ + "')";
    /** The sender (create permission) or the receiver (confirm permission) may call a transfer off. */
    private static final String CAN_CANCEL = "hasAnyAuthority('" + PermissionCatalog.TRANSFER_CREATE + "', '"
            + PermissionCatalog.TRANSFER_CONFIRM + "')";

    private final CreateTransferUseCase createTransferUseCase;
    private final GetTransferUseCase getTransferUseCase;
    private final ListTransfersUseCase listTransfersUseCase;
    private final ListInTransitUseCase listInTransitUseCase;
    private final ReceiveTransferUseCase receiveTransferUseCase;
    private final CancelTransferUseCase cancelTransferUseCase;
    private final GetTransferHistoryUseCase getTransferHistoryUseCase;

    @PostMapping
    @PreAuthorize(CAN_CREATE)
    @Operation(summary = "Send a transfer",
            description = "Creates the transfer as SUBMITTED and takes the goods out of the source stock at once. "
                    + "You must work at the source location. If the source has too little stock you get 409 and "
                    + "nothing changes. Sending the same transferId again returns the existing transfer.")
    public ResponseEntity<ApiResponse<TransferResponse>> createTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.ANY_LOCATION) boolean anyLocation) {
        TransferResponse created = TransferResponse.from(
                createTransferUseCase.createTransfer(request.toCommand(new TransferActor(userId, anyLocation))));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{transferId}").buildAndExpand(created.transferId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Transfer sent", created));
    }

    @GetMapping
    @PreAuthorize(CAN_READ)
    @Operation(summary = "List transfers",
            description = "Newest first. A location matches the source or the destination. Without a role that works across "
                    + "every location you only see your own location. hasDiscrepancy=true is the discrepancy review list. "
                    + "from is inclusive, to is exclusive (ISO-8601, for example 2026-10-01T00:00:00Z).")
    public ApiResponse<TransferPageResponse> listTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) TransferStatus status,
            @Parameter(description = "true: receipts with damaged or missing goods; false: the others; leave out for all")
            @RequestParam(required = false) Boolean hasDiscrepancy,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(TransferPageResponse.from(listTransfersUseCase.listTransfers(new TransferPageRequest(
                page, size, locationId, status, hasDiscrepancy, productId, from, to,
                new TransferActor(userId, anyLocation)))));
    }

    @GetMapping("/in-transit")
    @PreAuthorize(CAN_READ)
    @Operation(summary = "Goods in transit",
            description = "Per destination and product, the base quantity that has left the source and is still travelling. "
                    + "Derived from SUBMITTED transfers; it is not a stock balance.")
    public ApiResponse<InTransitResponse> listInTransit(
            @RequestParam(required = false) UUID locationId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(InTransitResponse.from(
                listInTransitUseCase.listInTransit(locationId, new TransferActor(userId, anyLocation))));
    }

    @GetMapping("/{transferId}")
    @PreAuthorize(CAN_READ)
    @Operation(summary = "Get a transfer",
            description = "Lines show the quantity sent and, once received, the received, damaged and missing quantities.")
    public ApiResponse<TransferResponse> getTransfer(
            @PathVariable UUID transferId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(TransferResponse.from(
                getTransferUseCase.getTransfer(transferId, new TransferActor(userId, anyLocation))));
    }

    @PostMapping("/{transferId}/receive")
    @PreAuthorize(CAN_CONFIRM)
    @Operation(summary = "Receive a SUBMITTED transfer",
            description = "You must work at the destination. Good quantity goes into stock; damaged and missing goods do not "
                    + "and mark the transfer hasDiscrepancy. A reason is required for damaged or missing goods.")
    public ApiResponse<TransferResponse> receiveTransfer(
            @PathVariable UUID transferId,
            @Valid @RequestBody ReceiveTransferRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok("Transfer received", TransferResponse.from(receiveTransferUseCase.receiveTransfer(
                request.toCommand(transferId, new TransferActor(userId, anyLocation)))));
    }

    @PostMapping("/{transferId}/cancel")
    @PreAuthorize(CAN_CANCEL)
    @Operation(summary = "Cancel a transfer",
            description = "Only a SUBMITTED transfer, by a user of the source or the destination. The goods go back into "
                    + "the source stock in full. A received transfer cannot be cancelled.")
    public ApiResponse<TransferResponse> cancelTransfer(
            @PathVariable UUID transferId,
            @Valid @RequestBody CancelTransferRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok("Transfer cancelled", TransferResponse.from(cancelTransferUseCase.cancelTransfer(
                new CancelTransferCommand(transferId, request.reason(), new TransferActor(userId, anyLocation)))));
    }

    @GetMapping("/{transferId}/history")
    @PreAuthorize(CAN_READ)
    @Operation(summary = "Transfer history", description = "Every step with the user and the time, oldest first.")
    public ApiResponse<List<TransferEventResponse>> getTransferHistory(
            @PathVariable UUID transferId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = TransferSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(getTransferHistoryUseCase.getTransferHistory(transferId,
                new TransferActor(userId, anyLocation)).stream().map(TransferEventResponse::from).toList());
    }
}