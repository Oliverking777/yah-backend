package tsgp.yah_enterprise.product.presentation.web;

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
import tsgp.yah_enterprise.product.application.port.in.*;
import tsgp.yah_enterprise.product.domain.exception.VariantCodeNotFoundException;
import tsgp.yah_enterprise.product.presentation.dto.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** Variants of products (what is stocked and sold) and their units. A product's variants are listed on the product. */
@RestController
@RequestMapping("/yah/api/v1/variants")
@RequiredArgsConstructor
@Tag(name = "Product variants", description = "Name variants and manage the units they are counted in")
public class VariantController {

    private final UpdateVariantUseCase updateVariantUseCase;
    private final ChangeVariantStatusUseCase changeVariantStatusUseCase;
    private final GetVariantsUseCase getVariantsUseCase;
    private final AssignUnitToVariantUseCase assignUnitToVariantUseCase;
    private final DefineVariantConversionUseCase defineVariantConversionUseCase;
    private final RemoveUnitFromVariantUseCase removeUnitFromVariantUseCase;
    private final DeactivateVariantUnitUseCase deactivateVariantUnitUseCase;
    private final ListVariantUnitsUseCase listVariantUnitsUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_READ + "')")
    @Operation(summary = "Read several variants", description = "ids separated by commas. Unknown ids are skipped.")
    public ApiResponse<List<VariantSummaryResponse>> getVariants(@RequestParam List<UUID> ids) {
        return ApiResponse.ok(getVariantsUseCase.getVariants(ids).stream().map(VariantSummaryResponse::from).toList());
    }

    @GetMapping("/by-sku/{sku}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_READ + "')")
    @Operation(summary = "Find a variant by SKU", description = "Any case. 404 when none matches.")
    public ApiResponse<VariantSummaryResponse> findBySku(@PathVariable String sku) {
        return ApiResponse.ok(getVariantsUseCase.findVariantBySku(sku).map(VariantSummaryResponse::from)
                .orElseThrow(() -> new VariantCodeNotFoundException(sku)));
    }

    @PutMapping("/{variantId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_UPDATE + "')")
    @Operation(summary = "Rename a variant", description = "Answers the whole product.")
    public ApiResponse<ProductResponse> updateVariant(@PathVariable UUID variantId,
                                                      @Valid @RequestBody UpdateVariantRequest request) {
        return ApiResponse.ok("Variant updated",
                ProductResponse.from(updateVariantUseCase.updateVariant(variantId, request.toCommand())));
    }

    @PostMapping("/{variantId}/activate")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_UPDATE + "')")
    @Operation(summary = "Activate a variant")
    public ApiResponse<ProductResponse> activate(@PathVariable UUID variantId) {
        return ApiResponse.ok("Variant activated",
                ProductResponse.from(changeVariantStatusUseCase.activateVariant(variantId)));
    }

    @PostMapping("/{variantId}/deactivate")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_UPDATE + "')")
    @Operation(summary = "Deactivate a variant",
            description = "The last active variant of an active product cannot be deactivated: deactivate the product.")
    public ApiResponse<ProductResponse> deactivate(@PathVariable UUID variantId) {
        return ApiResponse.ok("Variant deactivated",
                ProductResponse.from(changeVariantStatusUseCase.deactivateVariant(variantId)));
    }

    @GetMapping("/{variantId}/units")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_READ + "')")
    @Operation(summary = "List the units of a variant")
    public ApiResponse<List<VariantUnitResponse>> listUnits(@PathVariable UUID variantId) {
        return ApiResponse.ok(listVariantUnitsUseCase.listVariantUnits(variantId)
                .stream().map(VariantUnitResponse::from).toList());
    }

    @PostMapping("/{variantId}/units")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.UNIT_CREATE + "')")
    @Operation(summary = "Assign a unit to a variant")
    public ResponseEntity<ApiResponse<VariantUnitResponse>> assignUnit(
            @PathVariable UUID variantId, @Valid @RequestBody AssignUnitToVariantRequest request) {
        VariantUnitResponse unit = VariantUnitResponse.from(
                assignUnitToVariantUseCase.assignUnit(request.toCommand(variantId)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{unitId}").buildAndExpand(unit.unitId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Unit assigned", unit));
    }

    @PutMapping("/{variantId}/units/{unitId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.UNIT_UPDATE + "')")
    @Operation(summary = "Change the conversion factor of a unit of a variant")
    public ApiResponse<VariantUnitResponse> defineConversion(
            @PathVariable UUID variantId, @PathVariable UUID unitId,
            @Valid @RequestBody DefineVariantConversionRequest request) {
        return ApiResponse.ok("Conversion updated", VariantUnitResponse.from(
                defineVariantConversionUseCase.defineConversion(request.toCommand(variantId, unitId))));
    }

    @DeleteMapping("/{variantId}/units/{unitId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.UNIT_DEACTIVATE + "')")
    @Operation(summary = "Remove a non-base unit from a variant")
    public ResponseEntity<Void> removeUnit(@PathVariable UUID variantId, @PathVariable UUID unitId) {
        removeUnitFromVariantUseCase.removeUnit(variantId, unitId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{variantId}/units/{unitId}/deactivate")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.UNIT_DEACTIVATE + "')")
    @Operation(summary = "Deactivate a unit of a variant")
    public ApiResponse<VariantUnitResponse> deactivateUnit(@PathVariable UUID variantId, @PathVariable UUID unitId) {
        return ApiResponse.ok("Unit deactivated",
                VariantUnitResponse.from(deactivateVariantUnitUseCase.deactivateUnit(variantId, unitId)));
    }
}

