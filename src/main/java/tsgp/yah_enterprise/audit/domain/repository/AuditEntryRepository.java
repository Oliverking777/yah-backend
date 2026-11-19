package tsgp.yah_enterprise.audit.domain.repository;

import tsgp.yah_enterprise.audit.domain.model.AuditEntry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Append-only: there is save, never update or delete. */
public interface AuditEntryRepository {

    AuditEntry save(AuditEntry entry);

    Optional<AuditEntry> findById(UUID auditEntryId);

    /** Newest first. */
    PagedAuditEntries search(AuditSearchCriteria criteria, int page, int size);

    /** Oldest first: the story of one purchase, transfer or sale. */
    PagedAuditEntries findByEntity(String entityType, UUID entityId, int page, int size);

    /** Newest first. */
    PagedAuditEntries findByActor(UUID actorId, Instant from, Instant to, int page, int size);

    /** SUCCESS entries of business actions only (no AUTH.* and no AUDIT.* entries), newest first. */
    List<AuditEntry> findRecent(Set<UUID> locationIds, int limit);

    /** Newest first, at most limit rows. */
    List<AuditEntry> streamForExport(AuditSearchCriteria criteria, int limit);
}
