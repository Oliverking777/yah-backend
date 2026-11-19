package tsgp.yah_enterprise.category.presentation.web;

import java.net.URI;
import java.util.UUID;

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
import tsgp.yah_enterprise.category.application.dto.CategoryPageRequest;
import tsgp.yah_enterprise.category.application.port.in.ChangeCategoryStatusUseCase;
import tsgp.yah_enterprise.category.application.port.in.CreateCategoryUseCase;
import tsgp.yah_enterprise.category.application.port.in.GetCategoryUseCase;
import tsgp.yah_enterprise.category.application.port.in.ListCategoriesUseCase;
import tsgp.yah_enterprise.category.application.port.in.UpdateCategoryUseCase;
import tsgp.yah_enterprise.category.presentation.dto.CategoryPageResponse;
import tsgp.yah_enterprise.category.presentation.dto.CategoryResponse;
import tsgp.yah_enterprise.category.presentation.dto.CreateCategoryRequest;
import tsgp.yah_enterprise.category.presentation.dto.UpdateCategoryRequest;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;

/**
 * Note: our shared ApiResponse class has the same simple name as Swagger's @ApiResponse annotation,
 * so the annotation is written with its full package name below.
 */
@RestController
@RequestMapping("/yah/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Create, read, update, activate and deactivate product categories")
public class CategoryController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final ChangeCategoryStatusUseCase changeCategoryStatusUseCase;

    @PostMapping
    @Operation(summary = "Create a category",
            description = "Creates a new, active category. Names are unique, ignoring case.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid name or description",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "A category with this name already exists",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CATEGORY_CREATE + "')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse created =
                CategoryResponse.from(createCategoryUseCase.createCategory(request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{categoryId}")
                .buildAndExpand(created.categoryId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Category created", created));
    }

    @GetMapping
    @Operation(summary = "List categories",
            description = "Returns one page of categories, ordered by name. Page numbers start at 0.")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CATEGORY_READ + "')")
    public ApiResponse<CategoryPageResponse> listCategories(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1 to 100)") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(CategoryPageResponse.from(
                listCategoriesUseCase.listCategories(new CategoryPageRequest(page, size))));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get a category by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CATEGORY_READ + "')")
    public ApiResponse<CategoryResponse> getCategoryById(
            @Parameter(description = "Category id") @PathVariable UUID categoryId) {
        return ApiResponse.ok(CategoryResponse.from(getCategoryUseCase.getCategoryById(categoryId)));
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update a category",
            description = "Changes the name and/or description. Fields left out stay unchanged; "
                    + "an empty description clears it. Use the activate/deactivate endpoints for the status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Another category already uses this name",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CATEGORY_UPDATE + "')")
    public ApiResponse<CategoryResponse> updateCategory(
            @Parameter(description = "Category id") @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ApiResponse.ok("Category updated",
                CategoryResponse.from(updateCategoryUseCase.updateCategory(categoryId, request.toCommand())));
    }

    @PostMapping("/{categoryId}/activate")
    @Operation(summary = "Activate a category", description = "Makes the category available again.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category activated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "The category is already active",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CATEGORY_ACTIVATE + "')")
    public ApiResponse<CategoryResponse> activateCategory(
            @Parameter(description = "Category id") @PathVariable UUID categoryId) {
        return ApiResponse.ok("Category activated",
                CategoryResponse.from(changeCategoryStatusUseCase.activateCategory(categoryId)));
    }

    @PostMapping("/{categoryId}/deactivate")
    @Operation(summary = "Deactivate a category",
            description = "Retires the category. It is kept, not deleted, because products may reference it.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "The category is already inactive",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.CATEGORY_DEACTIVATE + "')")
    public ApiResponse<CategoryResponse> deactivateCategory(
            @Parameter(description = "Category id") @PathVariable UUID categoryId) {
        return ApiResponse.ok("Category deactivated",
                CategoryResponse.from(changeCategoryStatusUseCase.deactivateCategory(categoryId)));
    }
}