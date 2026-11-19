package tsgp.yah_enterprise.supplier.presentation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.supplier.application.dto.SupplierPageRequest;
import tsgp.yah_enterprise.supplier.application.port.in.*;
import tsgp.yah_enterprise.supplier.domain.model.SupplierStatus;
import tsgp.yah_enterprise.supplier.presentation.dto.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/yah/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Create, read, update, activate and deactivate suppliers")
public class SupplierController {

    private final CreateSupplierUseCase createSupplierUseCase;
    private final GetSupplierUseCase getSupplierUseCase;
    private final ListSuppliersUseCase listSuppliersUseCase;
    private final UpdateSupplierUseCase updateSupplierUseCase;
    private final ChangeSupplierStatusUseCase changeSupplierStatusUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.SUPPLIER_CREATE + "')")
    @Operation(summary = "Create a supplier")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse created = SupplierResponse.from(
                createSupplierUseCase.createSupplier(request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{supplierId}").buildAndExpand(created.supplierId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Supplier created", created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.SUPPLIER_READ + "')")
    @Operation(summary = "List suppliers")
    public ApiResponse<SupplierPageResponse> listSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SupplierStatus status) {
        return ApiResponse.ok(SupplierPageResponse.from(
                listSuppliersUseCase.listSuppliers(new SupplierPageRequest(page, size, search, status))));
    }

    @GetMapping("/{supplierId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.SUPPLIER_READ + "')")
    @Operation(summary = "Get a supplier by id")
    public ApiResponse<SupplierResponse> getSupplier(@PathVariable UUID supplierId) {
        return ApiResponse.ok(SupplierResponse.from(getSupplierUseCase.getSupplier(supplierId)));
    }

    @PutMapping("/{supplierId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.SUPPLIER_UPDATE + "')")
    @Operation(summary = "Update a supplier")
    public ApiResponse<SupplierResponse> updateSupplier(
            @PathVariable UUID supplierId,
            @Valid @RequestBody UpdateSupplierRequest request) {
        return ApiResponse.ok("Supplier updated",
                SupplierResponse.from(updateSupplierUseCase.updateSupplier(supplierId, request.toCommand())));
    }

    @PostMapping("/{supplierId}/activate")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.SUPPLIER_UPDATE + "')")
    @Operation(summary = "Activate a supplier")
    public ApiResponse<SupplierResponse> activate(@PathVariable UUID supplierId) {
        return ApiResponse.ok("Supplier activated",
                SupplierResponse.from(changeSupplierStatusUseCase.activateSupplier(supplierId)));
    }

    @PostMapping("/{supplierId}/deactivate")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.SUPPLIER_UPDATE + "')")
    @Operation(summary = "Deactivate a supplier")
    public ApiResponse<SupplierResponse> deactivate(@PathVariable UUID supplierId) {
        return ApiResponse.ok("Supplier deactivated",
                SupplierResponse.from(changeSupplierStatusUseCase.deactivateSupplier(supplierId)));
    }
}
