package tsgp.yah_enterprise.iam.presentation.web;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import tsgp.yah_enterprise.iam.application.dto.PermissionPageRequest;
import tsgp.yah_enterprise.iam.application.port.in.CreatePermissionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.DeletePermissionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.GetPermissionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.ListPermissionsUseCase;
import tsgp.yah_enterprise.iam.application.port.in.UpdatePermissionUseCase;
import tsgp.yah_enterprise.iam.presentation.dto.CreatePermissionRequest;
import tsgp.yah_enterprise.iam.presentation.dto.PermissionPageResponse;
import tsgp.yah_enterprise.iam.presentation.dto.PermissionResponse;
import tsgp.yah_enterprise.iam.presentation.dto.UpdatePermissionRequest;

/**
 * Note: our shared ApiResponse class has the same simple name as Swagger's @ApiResponse annotation,
 * so the annotation is written with its full package name below.
 */
@RestController
@RequestMapping("/yah/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Create, read, update and delete permissions")
public class PermissionController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final CreatePermissionUseCase createPermissionUseCase;
    private final UpdatePermissionUseCase updatePermissionUseCase;
    private final GetPermissionUseCase getPermissionUseCase;
    private final ListPermissionsUseCase listPermissionsUseCase;
    private final DeletePermissionUseCase deletePermissionUseCase;

    @PostMapping
    @Operation(summary = "Create a permission",
            description = "Creates a new permission. The code is trimmed and stored in upper case (e.g. ROLE_CREATE).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Permission created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid permission code",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "A permission with this code already exists",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PERMISSION_CREATE + "')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {
        PermissionResponse created =
                PermissionResponse.from(createPermissionUseCase.createPermission(request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{permissionId}")
                .buildAndExpand(created.permissionId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Permission created", created));
    }

    @GetMapping
    @Operation(summary = "List permissions",
            description = "Returns one page of permissions, ordered by code. Page numbers start at 0.")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PERMISSION_READ + "')")
    public ApiResponse<PermissionPageResponse> listPermissions(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1 to 100)") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(PermissionPageResponse.from(
                listPermissionsUseCase.listPermissions(new PermissionPageRequest(page, size))));
    }

    @GetMapping("/{permissionId}")
    @Operation(summary = "Get a permission by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permission found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permission not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PERMISSION_READ + "')")
    public ApiResponse<PermissionResponse> getPermissionById(
            @Parameter(description = "Permission id") @PathVariable UUID permissionId) {
        return ApiResponse.ok(PermissionResponse.from(getPermissionUseCase.getPermissionById(permissionId)));
    }

    @GetMapping("/by-code/{permissionCode}")
    @Operation(summary = "Get a permission by code",
            description = "The lookup is case-insensitive, so 'role_create' finds 'ROLE_CREATE'.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permission found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid permission code",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permission not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PERMISSION_READ + "')")
    public ApiResponse<PermissionResponse> getPermissionByCode(
            @Parameter(description = "Permission code", example = "ROLE_CREATE") @PathVariable String permissionCode) {
        return ApiResponse.ok(PermissionResponse.from(getPermissionUseCase.getPermissionByCode(permissionCode)));
    }

    @PutMapping("/{permissionId}")
    @Operation(summary = "Update a permission",
            description = "Replaces the permission code.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permission updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permission not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Another permission already uses this code",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PERMISSION_UPDATE + "')")
    public ApiResponse<PermissionResponse> updatePermission(
            @Parameter(description = "Permission id") @PathVariable UUID permissionId,
            @Valid @RequestBody UpdatePermissionRequest request) {
        return ApiResponse.ok("Permission updated",
                PermissionResponse.from(updatePermissionUseCase.updatePermission(permissionId, request.toCommand())));
    }

    @DeleteMapping("/{permissionId}")
    @Operation(summary = "Delete a permission")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Permission deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permission not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.PERMISSION_DELETE + "')")
    public ResponseEntity<Void> deletePermission(
            @Parameter(description = "Permission id") @PathVariable UUID permissionId) {
        deletePermissionUseCase.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }
}