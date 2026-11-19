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

/** Lives in Sales so the Customer module stays free of Sales. */
@RestController
@RequestMapping("/yah/api/v1/customers/{customerId}/orders")
@RequiredArgsConstructor
@Tag(name = "Orders and sales")
public class CustomerOrdersController {

    private final ListOrdersUseCase listOrdersUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.SALE_READ + "')")
    @Operation(summary = "Orders of a customer", description = "Newest first.")
    public ApiResponse<OrderPageResult> customerOrders(@PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.USER_ID) UUID userId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = SalesSecurity.ANY_LOCATION) boolean anyLocation) {
        return ApiResponse.ok(listOrdersUseCase.listOrders(new OrderPageRequest(page, size, null, null, customerId,
                null, null, null, new OrderActor(userId, anyLocation))));
    }
}
