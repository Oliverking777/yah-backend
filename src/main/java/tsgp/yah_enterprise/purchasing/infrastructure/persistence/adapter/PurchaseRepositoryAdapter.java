package tsgp.yah_enterprise.purchasing.infrastructure.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.purchasing.domain.model.Purchase;
import tsgp.yah_enterprise.purchasing.domain.model.PurchaseStatus;
import tsgp.yah_enterprise.purchasing.domain.repository.PagedIds;
import tsgp.yah_enterprise.purchasing.domain.repository.PagedPurchases;
import tsgp.yah_enterprise.purchasing.domain.repository.PurchaseRepository;
import tsgp.yah_enterprise.purchasing.infrastructure.persistence.entity.PurchaseJpaEntity;
import tsgp.yah_enterprise.purchasing.infrastructure.persistence.mapper.PurchasingMapper;
import tsgp.yah_enterprise.purchasing.infrastructure.persistence.repository.PurchaseItemJpaRepository;
import tsgp.yah_enterprise.purchasing.infrastructure.persistence.repository.PurchaseJpaRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PurchaseRepositoryAdapter implements PurchaseRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"));

    private final PurchaseJpaRepository jpa;
    private final PurchaseItemJpaRepository itemJpa;
    private final PurchasingMapper mapper;

    @Override
    public Purchase save(Purchase purchase) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(purchase)));
    }

    @Override
    public Optional<Purchase> findById(UUID purchaseId) {
        return jpa.findById(purchaseId).map(mapper::toDomain);
    }

    @Override
    public Optional<Purchase> findByIdForUpdate(UUID purchaseId) {
        return jpa.findForUpdate(purchaseId).map(mapper::toDomain);
    }

    @Override
    public Optional<Purchase> findByNumber(String purchaseNumber) {
        return jpa.findByPurchaseNumber(purchaseNumber).map(mapper::toDomain);
    }

    @Override
    public PagedPurchases findAll(int page, int size, UUID supplierId, UUID locationId, PurchaseStatus status,
                                  Boolean hasDiscrepancy, Instant from, Instant to) {
        Specification<PurchaseJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();
            if (supplierId != null) all.add(cb.equal(root.get("supplierId"), supplierId));
            if (locationId != null) all.add(cb.equal(root.get("destinationLocationId"), locationId));
            if (status != null) all.add(cb.equal(root.get("purchaseStatus"), status));
            if (hasDiscrepancy != null) all.add(cb.equal(root.get("discrepancy"), hasDiscrepancy));
            if (from != null) all.add(cb.greaterThanOrEqualTo(root.<Instant>get("createdAt"), from));
            if (to != null) all.add(cb.lessThan(root.<Instant>get("createdAt"), to));
            return cb.and(all.toArray(new Predicate[0]));
        };
        Page<PurchaseJpaEntity> found = jpa.findAll(spec, PageRequest.of(page, size, NEWEST_FIRST));
        return new PagedPurchases(found.getContent().stream().map(mapper::toDomain).toList(),
                found.getTotalElements());
    }

    @Override
    public PagedIds findDistinctProductIdsBySupplier(UUID supplierId, int page, int size) {
        Page<UUID> found = itemJpa.findDistinctProductIdsBySupplier(supplierId, PageRequest.of(page, size));
        return new PagedIds(found.getContent(), found.getTotalElements());
    }
}
