package tsgp.yah_enterprise.audit.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.audit.application.dto.*;
import tsgp.yah_enterprise.audit.application.port.in.*;
import tsgp.yah_enterprise.audit.domain.exception.*;
import tsgp.yah_enterprise.audit.domain.model.ActorType;
import tsgp.yah_enterprise.audit.domain.model.AuditEntry;
import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;
import tsgp.yah_enterprise.audit.domain.repository.AuditEntryRepository;
import tsgp.yah_enterprise.audit.domain.repository.AuditSearchCriteria;
import tsgp.yah_enterprise.audit.domain.repository.PagedAuditEntries;
import tsgp.yah_enterprise.audit.domain.valueobject.AuditAction;
import tsgp.yah_enterprise.audit.domain.valueobject.AuditActor;
import tsgp.yah_enterprise.audit.domain.valueobject.AuditChange;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Writes and reads the audit log. A SUCCESS entry joins the caller's transaction, so the log can never disagree with
 * the data. DENIED and FAILED entries use their own transaction, because the business transaction did not commit.
 */
@Service
@Transactional
public class AuditService implements RecordAuditEntryUseCase, GetAuditEntryUseCase, SearchAuditEntriesUseCase,
        GetEntityAuditHistoryUseCase, GetUserActivityHistoryUseCase, ExportAuditEntriesUseCase,
        GetRecentActivityUseCase {

    public static final int MAX_RANGE_DAYS = 366;
    public static final int MAX_EXPORT_ROWS = 10_000;
    public static final int MAX_RECENT = 50;
    static final String SYSTEM_ENTITY_USER = "USER";
    private static final Pattern ENTITY_TYPE = Pattern.compile("^[A-Z][A-Z0-9_]{1,39}$");
    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);
    private static final String CSV_HEADER = "auditEntryId,occurredAt,recordedAt,actorId,actorType,deviceId,action,"
            + "entityType,entityId,locationId,outcome,reason,correlationId,changes";

    private final AuditEntryRepository repository;
    private final RequiresNewTransaction requiresNew;

    public AuditService(AuditEntryRepository repository, RequiresNewTransaction requiresNew) {
        this.repository = repository;
        this.requiresNew = requiresNew;
    }

    // ------------------------------------------------------------------ write

    @Override
    public AuditEntryResult recordAuditEntry(RecordAuditEntryCommand c) {
        if (c == null) {
            throw new InvalidAuditEntryException("The audit entry is required");
        }
        AuditEntry entry = build(c);
        if (entry.getOutcome() == AuditOutcome.SUCCESS) {
            return toResult(repository.save(entry));
        }
        return toResult(requiresNew.run(() -> repository.save(entry)));
    }

    private static AuditEntry build(RecordAuditEntryCommand c) {
        List<AuditChange> changes = new ArrayList<>();
        for (AuditChangeCommand ch : c.changes() == null ? List.<AuditChangeCommand>of() : c.changes()) {
            if (ch == null) {
                throw new InvalidAuditEntryException("A changed field cannot be empty");
            }
            changes.add(new AuditChange(ch.fieldName(), ch.oldValue(), ch.newValue()));
        }
        return AuditEntry.create(new AuditActor(c.actorId(), c.actorType() == null ? ActorType.USER : c.actorType(),
                c.deviceId()), AuditAction.of(c.action()), c.entityType(), c.entityId(), c.locationId(),
                c.outcome(), c.reason(), changes, c.correlationId(), c.occurredAt());
    }

    // ------------------------------------------------------------------ read

    @Override
    @Transactional(readOnly = true)
    public AuditEntryResult getAuditEntry(UUID auditEntryId) {
        return toResult(repository.findById(auditEntryId).orElseThrow(() ->
                new AuditEntryNotFoundException("Audit entry " + auditEntryId + " not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public AuditPageResult searchAuditEntries(AuditSearchRequest r) {
        AuditSearchCriteria criteria = criteria(r.actorId(), r.action(), r.entityType(), r.entityId(),
                r.locationId(), r.outcome(), r.from(), r.to());
        PagedAuditEntries found = repository.search(criteria, r.page(), r.size());
        return page(found, r.page(), r.size());
    }

    @Override
    @Transactional(readOnly = true)
    public AuditPageResult getEntityAuditHistory(EntityHistoryRequest r) {
        if (r.entityType() == null || !ENTITY_TYPE.matcher(r.entityType()).matches() || r.entityId() == null) {
            throw new InvalidAuditQueryException("An entity type (upper case) and an entity id are required");
        }
        return page(repository.findByEntity(r.entityType(), r.entityId(), r.page(), r.size()), r.page(), r.size());
    }

    @Override
    public AuditPageResult getUserActivityHistory(UserActivityRequest r) {
        if (r.userId() == null) {
            throw new InvalidAuditQueryException("A user id is required");
        }
        requireRange(r.from(), r.to());
        PagedAuditEntries found = repository.findByActor(r.userId(), r.from(), r.to(), r.page(), r.size());
        // reading someone's activity is a sensitive access to the log: it is recorded
        selfAudit("AUDIT.USER_ACTIVITY_VIEWED", SYSTEM_ENTITY_USER, r.userId(), r.context(),
                List.of(change("from", r.from()), change("to", r.to())));
        return page(found, r.page(), r.size());
    }

    @Override
    public CsvExportResult exportAuditEntries(AuditExportRequest r) {
        AuditSearchCriteria criteria = criteria(r.actorId(), r.action(), r.entityType(), r.entityId(),
                r.locationId(), r.outcome(), r.from(), r.to());
        List<AuditEntry> rows = repository.streamForExport(criteria, MAX_EXPORT_ROWS + 1);
        if (rows.size() > MAX_EXPORT_ROWS) {
            throw new AuditExportTooLargeException("More than " + MAX_EXPORT_ROWS
                    + " entries match: narrow the filters");
        }
        StringBuilder csv = new StringBuilder(CSV_HEADER).append("\r\n");
        for (AuditEntry e : rows) {
            csv.append(csvRow(e)).append("\r\n");
        }
        List<AuditChangeCommand> filters = new ArrayList<>();
        addFilter(filters, "actorId", r.actorId());
        addFilter(filters, "action", r.action());
        addFilter(filters, "entityType", r.entityType());
        addFilter(filters, "entityId", r.entityId());
        addFilter(filters, "locationId", r.locationId());
        addFilter(filters, "outcome", r.outcome());
        addFilter(filters, "from", r.from());
        addFilter(filters, "to", r.to());
        filters.add(new AuditChangeCommand("rows", null, String.valueOf(rows.size())));
        selfAudit("AUDIT.EXPORTED", "AUDIT_EXPORT", null, r.context(), filters);
        return new CsvExportResult("audit-entries-" + FILE_TIME.format(Instant.now()) + ".csv", csv.toString(),
                rows.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEntryResult> getRecentActivity(Set<UUID> locationIds, int limit) {
        if (limit < 1 || limit > MAX_RECENT) {
            throw new InvalidAuditQueryException("Limit must be between 1 and " + MAX_RECENT);
        }
        return repository.findRecent(locationIds, limit).stream().map(AuditService::toResult).toList();
    }

    // ------------------------------------------------------------------ helpers

    /** Validates the filters and the range. A range is required unless the search is for one entity id. */
    private static AuditSearchCriteria criteria(UUID actorId, String action, String entityType, UUID entityId,
                                                UUID locationId, AuditOutcome outcome, Instant from, Instant to) {
        if (entityId == null) {
            requireRange(from, to);
        } else if (from != null || to != null) {
            requireRange(from == null ? Instant.EPOCH : from, to == null ? Instant.now().plusSeconds(1) : to);
        }
        if (entityType != null && !ENTITY_TYPE.matcher(entityType).matches()) {
            throw new InvalidAuditQueryException("Entity type must be upper case letters, digits or _");
        }
        String cleanedAction = action == null || action.isBlank() ? null : action.trim().toUpperCase(Locale.ROOT);
        return new AuditSearchCriteria(actorId, cleanedAction, entityType, entityId,
                locationId == null ? null : Set.of(locationId), outcome, from, to);
    }

    private static void requireRange(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new InvalidAuditQueryException("A period is required: send from and to");
        }
        if (!from.isBefore(to)) {
            throw new InvalidAuditQueryException("from must be before to");
        }
        if (Duration.between(from, to).compareTo(Duration.ofDays(MAX_RANGE_DAYS)) > 0) {
            throw new InvalidAuditQueryException("The period is at most " + MAX_RANGE_DAYS + " days");
        }
    }

    /** Audit writes its own entries (sensitive reads and exports) in the caller's transaction. */
    private void selfAudit(String action, String entityType, UUID entityId, AuditRequestContext ctx,
                           List<AuditChangeCommand> changes) {
        UUID actorId = ctx == null ? null : ctx.userId();
        repository.save(build(new RecordAuditEntryCommand(actorId, actorId == null ? ActorType.SYSTEM : ActorType.USER,
                ctx == null ? null : ctx.deviceId(), action, entityType, entityId, null, AuditOutcome.SUCCESS, null,
                changes, ctx == null || ctx.correlationId() == null ? UUID.randomUUID() : ctx.correlationId(), null)));
    }

    private static AuditChangeCommand change(String field, Object value) {
        return new AuditChangeCommand(field, null, value == null ? null : value.toString());
    }

    private static void addFilter(List<AuditChangeCommand> list, String field, Object value) {
        if (value != null) {
            list.add(change(field, value));
        }
    }

    private static AuditPageResult page(PagedAuditEntries found, int page, int size) {
        return AuditPageResult.of(found.items().stream().map(AuditService::toResult).toList(), page, size,
                found.total());
    }

    static AuditEntryResult toResult(AuditEntry e) {
        return new AuditEntryResult(e.getAuditEntryId(), e.getActor().actorId(), e.getActor().actorType().name(),
                e.getActor().deviceId(), e.getAction().value(), e.getEntityType(), e.getEntityId(), e.getLocationId(),
                e.getOutcome().name(), e.getReason(),
                e.getChanges().stream().map(c -> new AuditChangeResult(c.fieldName(), c.oldValue(), c.newValue()))
                        .toList(),
                e.getCorrelationId(), e.getOccurredAt(), e.getRecordedAt());
    }

    private static String csvRow(AuditEntry e) {
        String changes = e.getChanges().stream()
                .map(c -> c.fieldName() + ": " + nullToEmpty(c.oldValue()) + " -> " + nullToEmpty(c.newValue()))
                .collect(Collectors.joining("; "));
        return String.join(",",
                cell(e.getAuditEntryId()), cell(e.getOccurredAt()), cell(e.getRecordedAt()),
                cell(e.getActor().actorId()), cell(e.getActor().actorType()), cell(e.getActor().deviceId()),
                cell(e.getAction().value()), cell(e.getEntityType()), cell(e.getEntityId()), cell(e.getLocationId()),
                cell(e.getOutcome()), cell(e.getReason()), cell(e.getCorrelationId()), cell(changes));
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    /** CSV cell: quoted, quotes doubled, and a leading = + - @ is neutralised so a spreadsheet never runs it. */
    static String cell(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
