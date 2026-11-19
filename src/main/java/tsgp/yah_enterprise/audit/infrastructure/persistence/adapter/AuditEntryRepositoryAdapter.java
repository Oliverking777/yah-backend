package tsgp.yah_enterprise.audit.infrastructure.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.audit.domain.model.AuditEntry;
import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;
import tsgp.yah_enterprise.audit.domain.repository.AuditEntryRepository;
import tsgp.yah_enterprise.audit.domain.repository.AuditSearchCriteria;
import tsgp.yah_enterprise.audit.domain.repository.PagedAuditEntries;
import tsgp.yah_enterprise.audit.infrastructure.persistence.entity.AuditEntryJpaEntity;
import tsgp.yah_enterprise.audit.infrastructure.persistence.mapper.AuditEntryMapper;
import tsgp.yah_enterprise.audit.infrastructure.persistence.repository.AuditEntryJpaRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuditEntryRepositoryAdapter implements AuditEntryRepository {

    private static final Sort NEWEST_FIRST =
            Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("recordedAt"));
    private static final Sort OLDEST_FIRST =
            Sort.by(Sort.Order.asc("occurredAt"), Sort.Order.asc("recordedAt"));

    private final AuditEntryJpaRepository jpa;
    private final AuditEntryMapper mapper;

    @Override
    public AuditEntry save(AuditEntry entry) {
        // persist, not merge: an entry is only ever inserted
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(entry)));
    }

    @Override
    public Optional<AuditEntry> findById(UUID auditEntryId) {
        return jpa.findById(auditEntryId).map(mapper::toDomain);
    }

    @Override
    public PagedAuditEntries search(AuditSearchCriteria c, int page, int size) {
        return paged(jpa.findAll(spec(c), PageRequest.of(page, size, NEWEST_FIRST)));
    }

    @Override
    public PagedAuditEntries findByEntity(String entityType, UUID entityId, int page, int size) {
        Specification<AuditEntryJpaEntity> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("entityType"), entityType), cb.equal(root.get("entityId"), entityId));
        return paged(jpa.findAll(spec, PageRequest.of(page, size, OLDEST_FIRST)));
    }

    @Override
    public PagedAuditEntries findByActor(UUID actorId, Instant from, Instant to, int page, int size) {
        return paged(jpa.findAll(spec(new AuditSearchCriteria(actorId, null, null, null, null, null, from, to)),
                PageRequest.of(page, size, NEWEST_FIRST)));
    }

    @Override
    public List<AuditEntry> findRecent(Set<UUID> locationIds, int limit) {
        Specification<AuditEntryJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();
            all.add(cb.equal(root.get("outcome"), AuditOutcome.SUCCESS));
            all.add(cb.not(cb.like(root.<String>get("action"), "AUTH.%")));
            all.add(cb.not(cb.like(root.<String>get("action"), "AUDIT.%")));
            if (locationIds != null && !locationIds.isEmpty()) {
                all.add(root.get("locationId").in(locationIds));
            }
            return cb.and(all.toArray(new Predicate[0]));
        };
        return jpa.findAll(spec, PageRequest.of(0, limit, NEWEST_FIRST)).getContent().stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<AuditEntry> streamForExport(AuditSearchCriteria c, int limit) {
        return jpa.findAll(spec(c), PageRequest.of(0, limit, NEWEST_FIRST)).getContent().stream()
                .map(mapper::toDomain).toList();
    }

    private PagedAuditEntries paged(Page<AuditEntryJpaEntity> found) {
        return new PagedAuditEntries(found.getContent().stream().map(mapper::toDomain).toList(),
                found.getTotalElements());
    }

    private static Specification<AuditEntryJpaEntity> spec(AuditSearchCriteria c) {
        return (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();
            if (c.actorId() != null) all.add(cb.equal(root.get("actorId"), c.actorId()));
            if (c.action() != null) all.add(cb.equal(root.get("action"), c.action()));
            if (c.entityType() != null) all.add(cb.equal(root.get("entityType"), c.entityType()));
            if (c.entityId() != null) all.add(cb.equal(root.get("entityId"), c.entityId()));
            if (c.locationIds() != null && !c.locationIds().isEmpty()) {
                all.add(root.get("locationId").in(c.locationIds()));
            }
            if (c.outcome() != null) all.add(cb.equal(root.get("outcome"), c.outcome()));
            if (c.from() != null) all.add(cb.greaterThanOrEqualTo(root.<Instant>get("occurredAt"), c.from()));
            if (c.to() != null) all.add(cb.lessThan(root.<Instant>get("occurredAt"), c.to()));
            return cb.and(all.toArray(new Predicate[0]));
        };
    }
}
