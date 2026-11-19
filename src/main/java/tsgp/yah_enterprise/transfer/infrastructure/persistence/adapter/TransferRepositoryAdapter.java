package tsgp.yah_enterprise.transfer.infrastructure.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.transfer.domain.model.Transfer;
import tsgp.yah_enterprise.transfer.domain.model.TransferEvent;
import tsgp.yah_enterprise.transfer.domain.model.TransferStatus;
import tsgp.yah_enterprise.transfer.domain.repository.PagedTransfers;
import tsgp.yah_enterprise.transfer.domain.repository.TransferRepository;
import tsgp.yah_enterprise.transfer.domain.valueobject.TransferNumber;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.entity.TransferEventJpaEntity;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.entity.TransferItemJpaEntity;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.entity.TransferJpaEntity;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.mapper.TransferMapper;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.repository.TransferEventJpaRepository;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.repository.TransferJpaRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransferRepositoryAdapter implements TransferRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"));

    private final TransferJpaRepository jpa;
    private final TransferEventJpaRepository eventJpa;
    private final TransferMapper mapper;

    @Override
    public Transfer save(Transfer transfer) {
        TransferJpaEntity saved = jpa.saveAndFlush(mapper.toEntity(transfer));
        List<TransferEventJpaEntity> events = transfer.pendingEvents().stream().map(mapper::toEntity).toList();
        if (!events.isEmpty()) {
            eventJpa.saveAllAndFlush(events);
        }
        transfer.clearPendingEvents();
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Transfer> findById(UUID transferId) {
        return jpa.findById(transferId).map(mapper::toDomain);
    }

    @Override
    public Optional<Transfer> findByIdForUpdate(UUID transferId) {
        return jpa.findForUpdate(transferId).map(mapper::toDomain);
    }

    @Override
    public Optional<Transfer> findByNumber(TransferNumber number) {
        return jpa.findByTransferNumber(number.value()).map(mapper::toDomain);
    }

    @Override
    public PagedTransfers findAll(int page, int size, UUID locationId, TransferStatus status,
                                  Boolean hasDiscrepancy, UUID productId, Instant from, Instant to) {
        Specification<TransferJpaEntity> spec = filter(locationId, status, hasDiscrepancy, productId, from, to);
        Page<TransferJpaEntity> found = jpa.findAll(spec, PageRequest.of(page, size, NEWEST_FIRST));
        return new PagedTransfers(found.getContent().stream().map(mapper::toDomain).toList(),
                found.getTotalElements());
    }

    @Override
    public List<Transfer> findInTransit(UUID locationId) {
        return jpa.findAll(filter(locationId, TransferStatus.SUBMITTED, null, null, null, null), NEWEST_FIRST)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<TransferEvent> findEvents(UUID transferId) {
        return eventJpa.findAllByTransferIdOrderByOccurredAtAsc(transferId).stream().map(mapper::toDomain).toList();
    }

    private static Specification<TransferJpaEntity> filter(UUID locationId, TransferStatus status,
                                                           Boolean hasDiscrepancy, UUID productId,
                                                           Instant from, Instant to) {
        return (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();
            if (status != null) {
                all.add(cb.equal(root.get("transferStatus"), status));
            }
            if (hasDiscrepancy != null) {
                all.add(cb.equal(root.get("discrepancy"), hasDiscrepancy));
            }
            if (locationId != null) {
                all.add(cb.or(cb.equal(root.get("sourceLocationId"), locationId),
                        cb.equal(root.get("destinationLocationId"), locationId)));
            }
            if (from != null) {
                all.add(cb.greaterThanOrEqualTo(root.<Instant>get("createdAt"), from));
            }
            if (to != null) {
                all.add(cb.lessThan(root.<Instant>get("createdAt"), to));
            }
            if (productId != null) {
                // a transfer that has a line for this product (no join, so paging and counts stay exact)
                Subquery<UUID> lines = query.subquery(UUID.class);
                Root<TransferItemJpaEntity> item = lines.from(TransferItemJpaEntity.class);
                lines.select(item.<UUID>get("transferItemId"))
                        .where(cb.equal(item.get("transfer"), root), cb.equal(item.get("productId"), productId));
                all.add(cb.exists(lines));
            }
            return cb.and(all.toArray(new Predicate[0]));
        };
    }
}