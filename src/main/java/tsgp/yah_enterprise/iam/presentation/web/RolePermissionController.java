package tsgp.yah_enterprise.iam.presentation.web;

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
import org.springframework.web.bind.annotation.RestController;

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
import tsgp.yah_enterprise.iam.application.port.in.BulkAddRolePermissionsUseCase;
import tsgp.yah_enterprise.iam.application.port.in.BulkRemoveRolePermissionsUseCase;
import tsgp.yah_enterprise.iam.application.port.in.GetRolePermissionsUseCase;
import tsgp.yah_enterprise.iam.application.port.in.RemoveRolePermissionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.ReplaceRolePermissionsUseCase;
import tsgp.yah_enterprise.iam.presentation.dto.BulkRolePermissionsRequest;
import tsgp.yah_enterprise.iam.presentation.dto.ReplaceRolePermissionsRequest;
import tsgp.yah_enterprise.iam.presentation.dto.RolePermissionsResponse;

/**
 * Note: our shared ApiResponse class has the same simple name as Swagger's @ApiResponse annotation,
 * so the annotation is written with its full package name below.
 */
@RestController
@RequestMapping("/yah/api/v1/roles/{roleId}/permissions")
@RequiredArgsConstructor
@Tag(name = "Role permissions", description = "Manage which permissions a role grants")
public class RolePermissionController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final GetRolePermissionsUseCase getRolePermissionsUseCase;
    private final ReplaceRolePermissionsUseCase replaceRolePermissionsUseCase;
    private final BulkAddRolePermissionsUseCase bulkAddRolePermissionsUseCase;
    private final BulkRemoveRolePermissionsUseCase bulkRemoveRolePermissionsUseCase;
    private final RemoveRolePermissionUseCase removeRolePermissionUseCase;

    @GetMapping
    @Operation(summary = "List the permissions of a role")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permissions of the role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_PERMISSION_READ + "')")
    public ApiResponse<RolePermissionsResponse> getRolePermissions(
            @Parameter(description = "Role id") @PathVariable UUID roleId) {
        return ApiResponse.ok(RolePermissionsResponse.from(getRolePermissionsUseCase.getRolePermissions(roleId)));
    }

    @PutMapping
    @Operation(summary = "Set the permissions of a role",
            description = "Replaces the complete list of permissions of the role. "
                    + "Send an empty array to remove all of them.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permissions updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role or one of the permissions not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_PERMISSION_UPDATE + "')")
    public ApiResponse<RolePermissionsResponse> replaceRolePermissions(
            @Parameter(description = "Role id") @PathVariable UUID roleId,
            @Valid @RequestBody ReplaceRolePermissionsRequest request) {
        return ApiResponse.ok("Role permissions updated",
                RolePermissionsResponse.from(
                        replaceRolePermissionsUseCase.replaceRolePermissions(roleId, request.toCommand())));
    }

    @PostMapping
    @Operation(summary = "Add several permissions to a role",
            description = "Adds the given permissions and keeps the ones the role already has. "
                    + "Ids the role already has are ignored. All or nothing: if any id does not exist, nothing is added. "
                    + "Returns the complete list of permissions after the change.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permissions added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role or one of the permissions not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_PERMISSION_UPDATE + "')")
    public ApiResponse<RolePermissionsResponse> addRolePermissions(
            @Parameter(description = "Role id") @PathVariable UUID roleId,
            @Valid @RequestBody BulkRolePermissionsRequest request) {
        return ApiResponse.ok("Permissions added to role",
                RolePermissionsResponse.from(
                        bulkAddRolePermissionsUseCase.addRolePermissions(roleId, request.toCommand())));
    }

    @PostMapping("/remove")
    @Operation(summary = "Remove several permissions from a role",
            description = "Removes the given permissions and keeps the rest. "
                    + "Ids the role does not have are ignored. "
                    + "Returns the complete list of permissions after the change.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permissions removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_PERMISSION_UPDATE + "')")
    public ApiResponse<RolePermissionsResponse> removeRolePermissions(
            @Parameter(description = "Role id") @PathVariable UUID roleId,
            @Valid @RequestBody BulkRolePermissionsRequest request) {
        return ApiResponse.ok("Permissions removed from role",
                RolePermissionsResponse.from(
                        bulkRemoveRolePermissionsUseCase.removeRolePermissions(roleId, request.toCommand())));
    }

    @DeleteMapping("/{permissionId}")
    @Operation(summary = "Remove one permission from a role",
            description = "Does nothing (still 204) if the role does not have that permission.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Permission removed from the role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "An id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_PERMISSION_UPDATE + "')")
    public ResponseEntity<Void> removeRolePermission(
            @Parameter(description = "Role id") @PathVariable UUID roleId,
            @Parameter(description = "Permission id") @PathVariable UUID permissionId) {
        removeRolePermissionUseCase.removeRolePermission(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }
}