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
import tsgp.yah_enterprise.product.application.dto.ProductPageRequest;
import tsgp.yah_enterprise.product.application.port.in.*;
import tsgp.yah_enterprise.product.domain.model.ProductStatus;
import tsgp.yah_enterprise.product.presentation.dto.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/yah/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Manage products and their variants")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ChangeProductCategoryUseCase changeProductCategoryUseCase;
    private final ChangeProductStatusUseCase changeProductStatusUseCase;
    private final AddVariantUseCase addVariantUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_CREATE + "')")
    @Operation(summary = "Create a product with its variants",
            description = "All at once: the product, each variant and its units. The product code (PRD-000001) "
                    + "and the SKUs (PRD-000001-01...) are generated.")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = ProductResponse.from(
                createProductUseCase.createProduct(request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{productId}").buildAndExpand(created.productId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Product created", created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_READ + "')")
    @Operation(summary = "List products")
    public ApiResponse<ProductPageResponse> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProductStatus status) {
        return ApiResponse.ok(ProductPageResponse.from(
                listProductsUseCase.listProducts(new ProductPageRequest(page, size, search, status))));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_READ + "')")
    @Operation(summary = "Get a product by id")
    public ApiResponse<ProductResponse> getProduct(@PathVariable UUID productId) {
        return ApiResponse.ok(ProductResponse.from(getProductUseCase.getProduct(productId)));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_UPDATE + "')")
    @Operation(summary = "Update product name and description")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable UUID productId, @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.ok("Product updated",
                ProductResponse.from(updateProductUseCase.updateProduct(productId, request.toCommand())));
    }

    @PutMapping("/{productId}/category")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_UPDATE + "')")
    @Operation(summary = "Change product category")
    public ApiResponse<ProductResponse> changeCategory(
            @PathVariable UUID productId, @Valid @RequestBody ChangeProductCategoryRequest request) {
        return ApiResponse.ok("Category updated",
                ProductResponse.from(changeProductCategoryUseCase.changeProductCategory(
                        productId, request.categoryId())));
    }

    @PostMapping("/{productId}/activate")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_UPDATE + "')")
    @Operation(summary = "Activate a product")
    public ApiResponse<ProductResponse> activate(@PathVariable UUID productId) {
        return ApiResponse.ok("Product activated",
                ProductResponse.from(changeProductStatusUseCase.activateProduct(productId)));
    }

    @PostMapping("/{productId}/deactivate")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_UPDATE + "')")
    @Operation(summary = "Deactivate a product")
    public ApiResponse<ProductResponse> deactivate(@PathVariable UUID productId) {
        return ApiResponse.ok("Product deactivated",
                ProductResponse.from(changeProductStatusUseCase.deactivateProduct(productId)));
    }

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PRODUCT_UPDATE + "')")
    @Operation(summary = "Add a variant to a product",
            description = "With its units; its SKU is generated. A simple product's unnamed variant must be named "
                    + "first (PUT /variants/{variantId}). "
                    + "Answers the whole product.")
    public ResponseEntity<ApiResponse<ProductResponse>> addVariant(
            @PathVariable UUID productId, @Valid @RequestBody AddVariantRequest request) {
        ProductResponse product = ProductResponse.from(addVariantUseCase.addVariant(request.toCommand(productId)));
        return ResponseEntity.status(201).body(ApiResponse.created("Variant added", product));
    }
}
