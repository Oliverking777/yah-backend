package tsgp.yah_enterprise.pricing.presentation.web;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.pricing.application.dto.PricePageRequest;
import tsgp.yah_enterprise.pricing.application.port.in.ChangeProductPriceUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.CreateProductPriceUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.DeactivateProductPriceUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.GetApplicablePriceUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.GetProductPriceUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.ListPriceHistoryUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.ListProductPricesUseCase;
import tsgp.yah_enterprise.pricing.presentation.dto.ApplicablePriceResponse;
import tsgp.yah_enterprise.pricing.presentation.dto.ChangeProductPriceRequest;
import tsgp.yah_enterprise.pricing.presentation.dto.CreateProductPriceRequest;
import tsgp.yah_enterprise.pricing.presentation.dto.PricePageResponse;
import tsgp.yah_enterprise.pricing.presentation.dto.ProductPriceResponse;

/**
 * A price is a sub-resource of a product, so this controller is nested under it.
 * "history" and "applicable" are literal paths: Spring matches them before "/{priceId}".
 * <p>
 * Note: our shared ApiResponse class has the same simple name as Swagger's @ApiResponse annotation,
 * so the annotation is written with its full package name below.
 */
@RestController
@RequestMapping("/yah/api/v1/products/{productId}/prices")
@RequiredArgsConstructor
@Tag(name = "Prices", description = "Define, change and look up the prices of a product, per unit and quantity range")
public class ProductPriceController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final CreateProductPriceUseCase createProductPriceUseCase;
    private final ChangeProductPriceUseCase changeProductPriceUseCase;
    private final DeactivateProductPriceUseCase deactivateProductPriceUseCase;
    private final GetProductPriceUseCase getProductPriceUseCase;
    private final ListProductPricesUseCase listProductPricesUseCase;
    private final ListPriceHistoryUseCase listPriceHistoryUseCase;
    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRICE_RULE_CREATE + "')")
    @Operation(summary = "Create a price",
            description = "Creates an active price for one unit of the product. A flat price uses minimumQuantity 1 "
                    + "and no maximumQuantity. Two active prices of the same unit may not overlap in quantity and period.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Price created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid price, quantity range or period",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Missing permission",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Product or unit not priceable, or the price overlaps another one",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProductPriceResponse>> createPrice(
            @Parameter(description = "Product id") @PathVariable UUID productId,
            @Valid @RequestBody CreateProductPriceRequest request) {
        ProductPriceResponse created = ProductPriceResponse.from(
                createProductPriceUseCase.createProductPrice(request.toCommand(productId)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{priceId}")
                .buildAndExpand(created.priceId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Price created", created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRICE_RULE_READ + "')")
    @Operation(summary = "List current prices",
            description = "Returns the prices of the product that are active and in force right now. "
                    + "Use the unitId filter to see one unit only.")
    public ApiResponse<List<ProductPriceResponse>> listPrices(
            @Parameter(description = "Product id") @PathVariable UUID productId,
            @Parameter(description = "Only this unit (optional)") @RequestParam(required = false) UUID unitId) {
        return ApiResponse.ok(listProductPricesUseCase.listProductPrices(productId, unitId).stream()
                .map(ProductPriceResponse::from).toList());
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRICE_RULE_READ + "')")
    @Operation(summary = "Price history",
            description = "Returns one page of every price row of the product, active and closed, newest first. "
                    + "Page numbers start at 0.")
    public ApiResponse<PricePageResponse> listPriceHistory(
            @Parameter(description = "Product id") @PathVariable UUID productId,
            @Parameter(description = "Only this unit (optional)") @RequestParam(required = false) UUID unitId,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1 to 100)") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(PricePageResponse.from(
                listPriceHistoryUseCase.listPriceHistory(productId, unitId, new PricePageRequest(page, size))));
    }

    @GetMapping("/applicable")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRICE_RULE_READ + "')")
    @Operation(summary = "Get the applicable price",
            description = "Returns the unit price and the line total for a quantity of one unit. "
                    + "The price of another unit is never converted. Leave out 'at' to price at the current moment.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Price found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid quantity",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "This unit has no price for this quantity",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<ApplicablePriceResponse> getApplicablePrice(
            @Parameter(description = "Product id") @PathVariable UUID productId,
            @Parameter(description = "Unit id") @RequestParam UUID unitId,
            @Parameter(description = "Quantity (at least 1)") @RequestParam int quantity,
            @Parameter(description = "Moment to price at, ISO-8601 UTC (optional)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant at) {
        return ApiResponse.ok(ApplicablePriceResponse.from(
                getApplicablePriceUseCase.getApplicablePrice(productId, unitId, quantity, at)));
    }

    @GetMapping("/{priceId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRICE_RULE_READ + "')")
    @Operation(summary = "Get a price by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Price found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "An id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Price not found for this product",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<ProductPriceResponse> getPrice(
            @Parameter(description = "Product id") @PathVariable UUID productId,
            @Parameter(description = "Price id") @PathVariable UUID priceId) {
        return ApiResponse.ok(ProductPriceResponse.from(getProductPriceUseCase.getProductPrice(productId, priceId)));
    }

    @PutMapping("/{priceId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRICE_RULE_UPDATE + "')")
    @Operation(summary = "Change a price",
            description = "Changes the amount and/or the quantity range. The old price is closed and kept in the history; "
                    + "a new price is created and returned. Fields left out stay unchanged.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Price changed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input, or nothing to change",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Price not found for this product",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Price inactive, product or unit not priceable, or the new range overlaps another price",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<ProductPriceResponse> changePrice(
            @Parameter(description = "Product id") @PathVariable UUID productId,
            @Parameter(description = "Price id") @PathVariable UUID priceId,
            @Valid @RequestBody ChangeProductPriceRequest request) {
        return ApiResponse.ok("Price changed", ProductPriceResponse.from(
                changeProductPriceUseCase.changeProductPrice(productId, priceId, request.toCommand())));
    }

    @PostMapping("/{priceId}/deactivate")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRICE_RULE_DEACTIVATE + "')")
    @Operation(summary = "Deactivate a price",
            description = "Stops the price from applying. The row is kept for the history.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Price deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Price not found for this product",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "The price is already inactive",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<ProductPriceResponse> deactivatePrice(
            @Parameter(description = "Product id") @PathVariable UUID productId,
            @Parameter(description = "Price id") @PathVariable UUID priceId) {
        return ApiResponse.ok("Price deactivated", ProductPriceResponse.from(
                deactivateProductPriceUseCase.deactivateProductPrice(productId, priceId)));
    }
}
