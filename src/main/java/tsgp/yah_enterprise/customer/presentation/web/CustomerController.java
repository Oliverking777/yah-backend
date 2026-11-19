package tsgp.yah_enterprise.customer.presentation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.customer.application.dto.CustomerPageRequest;
import tsgp.yah_enterprise.customer.application.port.in.*;
import tsgp.yah_enterprise.customer.domain.model.CustomerStatus;
import tsgp.yah_enterprise.customer.presentation.dto.*;
import tsgp.yah_enterprise.config.ApiResponse;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/yah/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Create, read, update, activate and deactivate customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final ChangeCustomerStatusUseCase changeCustomerStatusUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CUSTOMER_CREATE + "')")
    @Operation(summary = "Create a customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse created = CustomerResponse.from(
                createCustomerUseCase.createCustomer(request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{customerId}").buildAndExpand(created.customerId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Customer created", created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CUSTOMER_READ + "')")
    @Operation(summary = "List customers")
    public ApiResponse<CustomerPageResponse> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CustomerStatus status) {
        return ApiResponse.ok(CustomerPageResponse.from(
                listCustomersUseCase.listCustomers(new CustomerPageRequest(page, size, search, status))));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CUSTOMER_READ + "')")
    @Operation(summary = "Get a customer by id")
    public ApiResponse<CustomerResponse> getCustomer(@PathVariable UUID customerId) {
        return ApiResponse.ok(CustomerResponse.from(getCustomerUseCase.getCustomer(customerId)));
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CUSTOMER_UPDATE + "')")
    @Operation(summary = "Update a customer")
    public ApiResponse<CustomerResponse> updateCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ApiResponse.ok("Customer updated",
                CustomerResponse.from(updateCustomerUseCase.updateCustomer(customerId, request.toCommand())));
    }

    @PostMapping("/{customerId}/activate")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CUSTOMER_UPDATE + "')")
    @Operation(summary = "Activate a customer")
    public ApiResponse<CustomerResponse> activateCustomer(@PathVariable UUID customerId) {
        return ApiResponse.ok("Customer activated",
                CustomerResponse.from(changeCustomerStatusUseCase.activateCustomer(customerId)));
    }

    @PostMapping("/{customerId}/deactivate")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CUSTOMER_UPDATE + "')")
    @Operation(summary = "Deactivate a customer")
    public ApiResponse<CustomerResponse> deactivateCustomer(@PathVariable UUID customerId) {
        return ApiResponse.ok("Customer deactivated",
                CustomerResponse.from(changeCustomerStatusUseCase.deactivateCustomer(customerId)));
    }
}
