package tsgp.yah_enterprise.location.presentation.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import tsgp.yah_enterprise.location.application.dto.LocationPageRequest;
import tsgp.yah_enterprise.location.application.port.in.AssignUserToLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.ChangeLocationStatusUseCase;
import tsgp.yah_enterprise.location.application.port.in.CreateLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.GetLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.GetUserLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.ListLocationUsersUseCase;
import tsgp.yah_enterprise.location.application.port.in.ListLocationsUseCase;
import tsgp.yah_enterprise.location.application.port.in.RemoveUserFromLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.UpdateLocationUseCase;
import tsgp.yah_enterprise.location.presentation.dto.AssignUserToLocationRequest;
import tsgp.yah_enterprise.location.presentation.dto.CreateLocationRequest;
import tsgp.yah_enterprise.location.presentation.dto.LocationPageResponse;
import tsgp.yah_enterprise.location.presentation.dto.LocationResponse;
import tsgp.yah_enterprise.location.presentation.dto.LocationUserResponse;
import tsgp.yah_enterprise.location.presentation.dto.UpdateLocationRequest;

/**
 * Note: our shared ApiResponse class has the same simple name as Swagger's @ApiResponse annotation,
 * so the annotation is written with its full package name below.
 */
@RestController
@RequestMapping("/yah/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Manage warehouses and stores, and the users assigned to them")
public class LocationController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final CreateLocationUseCase createLocationUseCase;
    private final UpdateLocationUseCase updateLocationUseCase;
    private final GetLocationUseCase getLocationUseCase;
    private final ListLocationsUseCase listLocationsUseCase;
    private final ChangeLocationStatusUseCase changeLocationStatusUseCase;
    private final AssignUserToLocationUseCase assignUserToLocationUseCase;
    private final RemoveUserFromLocationUseCase removeUserFromLocationUseCase;
    private final ListLocationUsersUseCase listLocationUsersUseCase;
    private final GetUserLocationUseCase getUserLocationUseCase;

    // ---------- locations ----------

    @PostMapping
    @Operation(summary = "Create a location",
            description = "Creates a new, active warehouse or store. Its code is generated (LOC-0001, LOC-0002...) "
                    + "and never changes. The address is optional.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Location created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid name, type or address",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.LOCATION_CREATE + "')")
    public ResponseEntity<ApiResponse<LocationResponse>> createLocation(
            @Valid @RequestBody CreateLocationRequest request) {
        LocationResponse created =
                LocationResponse.from(createLocationUseCase.createLocation(request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{locationId}")
                .buildAndExpand(created.locationId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Location created", created));
    }

    @GetMapping
    @Operation(summary = "List locations",
            description = "Returns one page of locations, ordered by name. Page numbers start at 0.")
    @PreAuthorize("hasAuthority('" + PermissionCatalog.LOCATION_READ + "')")
    public ApiResponse<LocationPageResponse> listLocations(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1 to 100)") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(LocationPageResponse.from(
                listLocationsUseCase.listLocations(new LocationPageRequest(page, size))));
    }

    /**
     * Declared before "/{locationId}" for readability; Spring always prefers the literal "/me" anyway.
     * The user id is read from the logged-in user with a method call ("userId()") because AuthenticatedUser
     * is a record. Using the expression keeps this module from importing IAM's AuthenticatedUser class.
     */
    @GetMapping("/me")
    @Operation(summary = "Get my location",
            description = "Returns the location the logged-in user is assigned to. "
                    + "The response has no data if the user has no location (for example an owner).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "The user's location, or no data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not logged in, or the session has ended",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<LocationResponse> getMyLocation(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId()") UUID userId) {
        return ApiResponse.ok(getUserLocationUseCase.findLocationOfUser(userId)
                .map(LocationResponse::from)
                .orElse(null));
    }

    @GetMapping("/{locationId}")
    @Operation(summary = "Get a location by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Location found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Location not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.LOCATION_READ + "')")
    public ApiResponse<LocationResponse> getLocationById(
            @Parameter(description = "Location id") @PathVariable UUID locationId) {
        return ApiResponse.ok(LocationResponse.from(getLocationUseCase.getLocationById(locationId)));
    }

    @PutMapping("/{locationId}")
    @Operation(summary = "Update a location",
            description = "Changes the name, type and/or address. Fields left out stay unchanged; "
                    + "an empty address removes it. The code cannot be changed; use the activate/deactivate "
                    + "endpoints for the status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Location updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Location not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.LOCATION_UPDATE + "')")
    public ApiResponse<LocationResponse> updateLocation(
            @Parameter(description = "Location id") @PathVariable UUID locationId,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ApiResponse.ok("Location updated",
                LocationResponse.from(updateLocationUseCase.updateLocation(locationId, request.toCommand())));
    }

    @PostMapping("/{locationId}/activate")
    @Operation(summary = "Activate a location", description = "Makes the location operational.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Location activated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Location not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "The location is already active",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.LOCATION_ACTIVATE + "')")
    public ApiResponse<LocationResponse> activateLocation(
            @Parameter(description = "Location id") @PathVariable UUID locationId) {
        return ApiResponse.ok("Location activated",
                LocationResponse.from(changeLocationStatusUseCase.activateLocation(locationId)));
    }

    @PostMapping("/{locationId}/deactivate")
    @Operation(summary = "Deactivate a location",
            description = "Temporarily disables the location. Its user assignments are kept.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Location deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Location not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "The location is already inactive",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.LOCATION_DEACTIVATE + "')")
    public ApiResponse<LocationResponse> deactivateLocation(
            @Parameter(description = "Location id") @PathVariable UUID locationId) {
        return ApiResponse.ok("Location deactivated",
                LocationResponse.from(changeLocationStatusUseCase.deactivateLocation(locationId)));
    }

    // ---------- users of a location ----------

    @GetMapping("/{locationId}/users")
    @Operation(summary = "List the users of a location",
            description = "Returns the users assigned to the location, oldest assignment first.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Location not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.LOCATION_USER_READ + "')")
    public ApiResponse<List<LocationUserResponse>> listLocationUsers(
            @Parameter(description = "Location id") @PathVariable UUID locationId) {
        return ApiResponse.ok(listLocationUsersUseCase.listLocationUsers(locationId).stream()
                .map(LocationUserResponse::from)
                .toList());
    }

    @PostMapping("/{locationId}/users")
    @Operation(summary = "Assign a user to a location",
            description = "A user belongs to at most one location. The location and the user must both be active.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User assigned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Location or user not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "The location or the user is inactive, or the user already has a location",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.LOCATION_USER_ASSIGN + "')")
    public ResponseEntity<ApiResponse<LocationUserResponse>> assignUser(
            @Parameter(description = "Location id") @PathVariable UUID locationId,
            @Valid @RequestBody AssignUserToLocationRequest request) {
        LocationUserResponse assigned = LocationUserResponse.from(
                assignUserToLocationUseCase.assignUser(locationId, request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(assigned.userId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("User assigned to location", assigned));
    }

    @DeleteMapping("/{locationId}/users/{userId}")
    @Operation(summary = "Remove a user from a location")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "An id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Location not found, or the user is not assigned to it",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.LOCATION_USER_REMOVE + "')")
    public ResponseEntity<Void> removeUser(
            @Parameter(description = "Location id") @PathVariable UUID locationId,
            @Parameter(description = "User id") @PathVariable UUID userId) {
        removeUserFromLocationUseCase.removeUser(locationId, userId);
        return ResponseEntity.noContent().build();
    }
}