package tsgp.yah_enterprise.iam.presentation.web;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import tsgp.yah_enterprise.iam.application.dto.RolePageRequest;
import tsgp.yah_enterprise.iam.application.port.in.*;
import tsgp.yah_enterprise.iam.presentation.dto.*;

/**
 * Note: our shared ApiResponse class has the same simple name as Swagger's @ApiResponse annotation,
 * so the annotation is written with its full package name below.
 */
@RestController
@RequestMapping("/yah/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Create, read, update and delete roles")
public class RoleController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final CreateRoleUseCase createRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final GetRoleUseCase getRoleUseCase;
    private final ListRolesUseCase listRolesUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;

    @PostMapping
    @Operation(summary = "Create a role",
            description = "Creates a new role. The name is trimmed and stored in upper case.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Role created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid name or description",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "A role with this name already exists",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_CREATE + "')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse created = RoleResponse.from(createRoleUseCase.createRole(request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{roleId}")
                .buildAndExpand(created.roleId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Role created", created));
    }

    @GetMapping
    @Operation(summary = "List roles",
            description = "Returns one page of roles, ordered by name. Page numbers start at 0.")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_READ + "')")
    public ApiResponse<RolePageResponse> listRoles(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1 to 100)") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(RolePageResponse.from(listRolesUseCase.listRoles(new RolePageRequest(page, size))));
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "Get a role by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_READ + "')")
    public ApiResponse<RoleResponse> getRoleById(@Parameter(description = "Role id") @PathVariable UUID roleId) {
        return ApiResponse.ok(RoleResponse.from(getRoleUseCase.getRoleById(roleId)));
    }

    @GetMapping("/by-name/{roleName}")
    @Operation(summary = "Get a role by name",
            description = "The lookup is case-insensitive, so 'admin' finds 'ADMIN'.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid role name",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_READ + "')")
    public ApiResponse<RoleResponse> getRoleByName(@Parameter(description = "Role name", example = "ADMIN")
                                                   @PathVariable String roleName) {
        return ApiResponse.ok(RoleResponse.from(getRoleUseCase.getRoleByName(roleName)));
    }

    @PatchMapping("/{roleId}")
    @Operation(summary = "Update a role",
            description = "Partial update: a field that is left out (null) stays unchanged. "
                    + "Send an empty description to clear it.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Another role already uses this name",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_UPDATE + "')")
    public ApiResponse<RoleResponse> updateRole(@Parameter(description = "Role id") @PathVariable UUID roleId,
                                                @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.ok("Role updated",
                RoleResponse.from(updateRoleUseCase.updateRole(roleId, request.toCommand())));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete a role")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Role deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_DELETE + "')")
    public ResponseEntity<Void> deleteRole(@Parameter(description = "Role id") @PathVariable UUID roleId) {
        deleteRoleUseCase.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }
}