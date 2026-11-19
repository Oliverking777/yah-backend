package tsgp.yah_enterprise.unit.presentation.web;

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
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.unit.application.dto.UnitPageRequest;
import tsgp.yah_enterprise.unit.application.port.in.ChangeUnitStatusUseCase;
import tsgp.yah_enterprise.unit.application.port.in.CreateUnitUseCase;
import tsgp.yah_enterprise.unit.application.port.in.GetUnitUseCase;
import tsgp.yah_enterprise.unit.application.port.in.ListUnitsUseCase;
import tsgp.yah_enterprise.unit.application.port.in.UpdateUnitUseCase;
import tsgp.yah_enterprise.unit.presentation.dto.CreateUnitRequest;
import tsgp.yah_enterprise.unit.presentation.dto.UnitPageResponse;
import tsgp.yah_enterprise.unit.presentation.dto.UnitResponse;
import tsgp.yah_enterprise.unit.presentation.dto.UpdateUnitRequest;

/**
 * Note: our shared ApiResponse class has the same simple name as Swagger's @ApiResponse annotation,
 * so the annotation is written with its full package name below.
 */
@RestController
@RequestMapping("/yah/api/v1/units")
@RequiredArgsConstructor
@Tag(name = "Units", description = "Create, read, rename, activate and deactivate units of measure")
public class UnitController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final CreateUnitUseCase createUnitUseCase;
    private final UpdateUnitUseCase updateUnitUseCase;
    private final GetUnitUseCase getUnitUseCase;
    private final ListUnitsUseCase listUnitsUseCase;
    private final ChangeUnitStatusUseCase changeUnitStatusUseCase;

    @PostMapping
    @Operation(summary = "Create a unit",
            description = "Creates a new, active unit. The name is unique ignoring case. The code is generated "
                    + "(U0001, U0002...) and never changes.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Unit created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid name",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "A unit with this name already exists",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.UNIT_CREATE + "')")
    public ResponseEntity<ApiResponse<UnitResponse>> createUnit(@Valid @RequestBody CreateUnitRequest request) {
        UnitResponse created = UnitResponse.from(createUnitUseCase.createUnit(request.toCommand()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{unitId}")
                .buildAndExpand(created.unitId())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.created("Unit created", created));
    }

    @GetMapping
    @Operation(summary = "List units",
            description = "Returns one page of units, ordered by name. Page numbers start at 0. Use status to see only ACTIVE or INACTIVE units.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page of units"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Status is not ACTIVE or INACTIVE",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.UNIT_READ + "')")
    public ApiResponse<UnitPageResponse> listUnits(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1 to 100)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Only units with this status (ACTIVE or INACTIVE), optional")
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(UnitPageResponse.from(
                listUnitsUseCase.listUnits(new UnitPageRequest(page, size, status))));
    }

    @GetMapping("/{unitId}")
    @Operation(summary = "Get a unit by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unit found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The id is not a valid UUID",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.UNIT_READ + "')")
    public ApiResponse<UnitResponse> getUnitById(@Parameter(description = "Unit id") @PathVariable UUID unitId) {
        return ApiResponse.ok(UnitResponse.from(getUnitUseCase.getUnitById(unitId)));
    }

    @PutMapping("/{unitId}")
    @Operation(summary = "Rename a unit",
            description = "Changes the name. The code never changes. Use the activate/deactivate endpoints for the status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unit updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid name",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Another unit already uses this name",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.UNIT_UPDATE + "')")
    public ApiResponse<UnitResponse> updateUnit(
            @Parameter(description = "Unit id") @PathVariable UUID unitId,
            @Valid @RequestBody UpdateUnitRequest request) {
        return ApiResponse.ok("Unit updated",
                UnitResponse.from(updateUnitUseCase.updateUnit(unitId, request.toCommand())));
    }

    @PostMapping("/{unitId}/activate")
    @Operation(summary = "Activate a unit", description = "Makes the unit available for new assignments again.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unit activated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "The unit is already active",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.UNIT_UPDATE + "')")
    public ApiResponse<UnitResponse> activateUnit(@Parameter(description = "Unit id") @PathVariable UUID unitId) {
        return ApiResponse.ok("Unit activated", UnitResponse.from(changeUnitStatusUseCase.activateUnit(unitId)));
    }

    @PostMapping("/{unitId}/deactivate")
    @Operation(summary = "Deactivate a unit",
            description = "Retires the unit. It is kept, not deleted. It only blocks new assignments: products already using it keep working.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unit deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "The unit is already inactive",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasAuthority('" + PermissionCatalog.UNIT_DEACTIVATE + "')")
    public ApiResponse<UnitResponse> deactivateUnit(@Parameter(description = "Unit id") @PathVariable UUID unitId) {
        return ApiResponse.ok("Unit deactivated",
                UnitResponse.from(changeUnitStatusUseCase.deactivateUnit(unitId)));
    }
}