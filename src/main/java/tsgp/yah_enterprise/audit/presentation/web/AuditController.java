package tsgp.yah_enterprise.audit.presentation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.config.PermissionCatalog;
import tsgp.yah_enterprise.audit.application.dto.*;
import tsgp.yah_enterprise.audit.application.port.in.*;
import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/** Read-only. The log is written by the other modules, never through this API. */
@RestController
@RequestMapping("/yah/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Search and read the audit log")
public class AuditController {

    private static final String CAN_VIEW = "hasAuthority('" + PermissionCatalog.AUDIT_READ + "')";
    private static final String CAN_EXPORT = "hasAuthority('" + PermissionCatalog.AUDIT_EXPORT + "')";
    private static final String USER_ID = "userId()";

    private final SearchAuditEntriesUseCase searchUseCase;
    private final GetAuditEntryUseCase getUseCase;
    private final GetEntityAuditHistoryUseCase entityHistoryUseCase;
    private final GetUserActivityHistoryUseCase userActivityUseCase;
    private final ExportAuditEntriesUseCase exportUseCase;

    @GetMapping("/entries")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Search the audit log (a from/to range of at most 366 days is required)")
    public ResponseEntity<ApiResponse<AuditPageResult>> search(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) AuditOutcome outcome,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Audit entries", searchUseCase.searchAuditEntries(
                new AuditSearchRequest(actorId, action, entityType, entityId, locationId, outcome, from, to,
                        page, size))));
    }

    // declared before /entries/{auditEntryId}: the literal path wins
    @GetMapping(value = "/entries/export", produces = "text/csv")
    @PreAuthorize(CAN_EXPORT)
    @Operation(summary = "Export the matching entries as CSV (at most 10,000 rows). The export is itself audited")
    public ResponseEntity<byte[]> export(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID userId,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) AuditOutcome outcome,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        CsvExportResult csv = exportUseCase.exportAuditEntries(new AuditExportRequest(actorId, action, entityType,
                entityId, locationId, outcome, from, to, context(userId, deviceId, correlationId)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csv.fileName() + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.content().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/entries/{auditEntryId}")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "Read one audit entry")
    public ResponseEntity<ApiResponse<AuditEntryResult>> get(@PathVariable UUID auditEntryId) {
        return ResponseEntity.ok(ApiResponse.ok("Audit entry", getUseCase.getAuditEntry(auditEntryId)));
    }

    @GetMapping("/entities/{entityType}/{entityId}")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "The full history of one entity, oldest first")
    public ResponseEntity<ApiResponse<AuditPageResult>> entityHistory(
            @PathVariable String entityType, @PathVariable UUID entityId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Entity history", entityHistoryUseCase.getEntityAuditHistory(
                new EntityHistoryRequest(entityType, entityId, page, size))));
    }

    @GetMapping("/users/{userId}/activity")
    @PreAuthorize(CAN_VIEW)
    @Operation(summary = "What one user did. Viewing it is itself audited")
    public ResponseEntity<ApiResponse<AuditPageResult>> userActivity(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = USER_ID) UUID callerId,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("User activity", userActivityUseCase.getUserActivityHistory(
                new UserActivityRequest(userId, from, to, page, size, context(callerId, deviceId, correlationId)))));
    }

    private static AuditRequestContext context(UUID userId, String deviceId, String correlationId) {
        UUID correlation;
        try {
            correlation = correlationId == null ? UUID.randomUUID() : UUID.fromString(correlationId.trim());
        } catch (IllegalArgumentException ex) {
            correlation = UUID.randomUUID();
        }
        return new AuditRequestContext(userId, deviceId, correlation);
    }
}
