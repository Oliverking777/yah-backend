package tsgp.yah_enterprise.iam.presentation.web;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import tsgp.yah_enterprise.config.RoleCatalog;
import tsgp.yah_enterprise.iam.application.dto.AuthenticatedUser;
import tsgp.yah_enterprise.iam.application.dto.UserPageRequest;
import tsgp.yah_enterprise.iam.application.port.in.ChangeUserPasswordUseCase;
import tsgp.yah_enterprise.iam.application.port.in.ChangeUserStatusUseCase;
import tsgp.yah_enterprise.iam.application.port.in.CreateUserUseCase;
import tsgp.yah_enterprise.iam.application.port.in.DeleteUserUseCase;
import tsgp.yah_enterprise.iam.application.port.in.GetUserUseCase;
import tsgp.yah_enterprise.iam.application.port.in.ListUsersUseCase;
import tsgp.yah_enterprise.iam.application.port.in.UpdateUserUseCase;
import tsgp.yah_enterprise.iam.presentation.dto.ChangeUserPasswordRequest;
import tsgp.yah_enterprise.iam.presentation.dto.CreateUserRequest;
import tsgp.yah_enterprise.iam.presentation.dto.UpdateUserRequest;
import tsgp.yah_enterprise.iam.presentation.dto.UserPageResponse;
import tsgp.yah_enterprise.iam.presentation.dto.UserResponse;

/**
 * Note: our shared ApiResponse class has the same simple name as Swagger's @ApiResponse annotation,
 * so the annotation is written with its full package name below.
 */
@RestController
@RequestMapping("/yah/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Create, read, update, activate, deactivate and delete users")
public class UserController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final ChangeUserStatusUseCase changeUserStatusUseCase;
    private final ChangeUserPasswordUseCase changeUserPasswordUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @PostMapping
    @Operation(summary = "Create a user",
            description = "Creates a new, active user with the given role and password. "
                    + "The login name is generated from the first and last name.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid name or password",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "The role does not exist",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.USER_CREATE + "')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = UserResponse.from(createUserUseCase.createUser(request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(created.userId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("User created", created));
    }

    @GetMapping
    @Operation(summary = "List users",
            description = "Returns one page of users. Page numbers start at 0.")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.USER_READ + "')")
    public ApiResponse<UserPageResponse> listUsers(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1 to 100)") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(UserPageResponse.from(listUsersUseCase.listUsers(new UserPageRequest(page, size))));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a user by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.USER_READ + "')")
    public ApiResponse<UserResponse> getUserById(@Parameter(description = "User id") @PathVariable UUID userId) {
        return ApiResponse.ok(UserResponse.from(getUserUseCase.getUserById(userId)));
    }

    @GetMapping("/by-username/{userName}")
    @Operation(summary = "Get a user by login name",
            description = "The lookup is case-insensitive, so 'John.Doe' finds 'john.doe'.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid login name",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.USER_READ + "')")
    public ApiResponse<UserResponse> getUserByUserName(
            @Parameter(description = "Login name", example = "john.doe") @PathVariable String userName) {
        return ApiResponse.ok(UserResponse.from(getUserUseCase.getUserByUserName(userName)));
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Update a user",
            description = "Partial update: a field that is left out (null) stays unchanged. "
                    + "Changing the role also needs the permission to assign roles. "
                    + "The login name cannot be changed.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Changing the role needs the permission to assign roles",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or role not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.USER_UPDATE + "')")
    public ApiResponse<UserResponse> updateUser(
            @Parameter(description = "User id") @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser currentUser) {
        if (request.roleId() != null && !currentUser.hasPermission(PermissionCatalog.USER_ASSIGN_ROLE)) {
            throw new AccessDeniedException("Changing a user's role needs the " + PermissionCatalog.USER_ASSIGN_ROLE
                    + " permission");
        }
        return ApiResponse.ok("User updated",
                UserResponse.from(updateUserUseCase.updateUser(userId, request.toCommand())));
    }

    @PostMapping("/{userId}/activate")
    @Operation(summary = "Activate a user", description = "Lets the user log in again. Does nothing if already active.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User activated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.USER_ACTIVATE + "')")
    public ApiResponse<UserResponse> activateUser(@Parameter(description = "User id") @PathVariable UUID userId) {
        return ApiResponse.ok("User activated", UserResponse.from(changeUserStatusUseCase.activateUser(userId)));
    }

    @PostMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate a user",
            description = "Only the owner can do this. Stops the user from logging in and rejects their current "
                    + "session. Does nothing if already inactive.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.USER_DEACTIVATE + "') and hasAuthority('"
            + RoleCatalog.OWNER_AUTHORITY + "')")
    public ApiResponse<UserResponse> deactivateUser(@Parameter(description = "User id") @PathVariable UUID userId) {
        return ApiResponse.ok("User deactivated", UserResponse.from(changeUserStatusUseCase.deactivateUser(userId)));
    }

    @PutMapping("/{userId}/password")
    @Operation(summary = "Set a user's password",
            description = "Replaces the user's password without needing the old one (an administrator action). "
                    + "Users change their own password with PUT /auth/me/password.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Password changed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The new password breaks the rules",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.USER_PASSWORD_RESET + "')")
    public ResponseEntity<Void> changeUserPassword(
            @Parameter(description = "User id") @PathVariable UUID userId,
            @Valid @RequestBody ChangeUserPasswordRequest request) {
        changeUserPasswordUseCase.changePassword(userId, request.toCommand());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user",
            description = "Only the owner can do this. Permanently deletes the user, their credential and their "
                    + "session. Consider deactivating instead, so the history stays.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.USER_DELETE + "') and hasAuthority('"
            + RoleCatalog.OWNER_AUTHORITY + "')")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "User id") @PathVariable UUID userId) {
        deleteUserUseCase.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}