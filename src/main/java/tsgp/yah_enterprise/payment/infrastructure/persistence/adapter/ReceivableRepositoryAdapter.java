package tsgp.yah_enterprise.payment.infrastructure.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.payment.domain.model.*;
import tsgp.yah_enterprise.payment.domain.repository.*;
import tsgp.yah_enterprise.payment.infrastructure.persistence.entity.*;
import tsgp.yah_enterprise.payment.infrastructure.persistence.mapper.PaymentMapper;
import tsgp.yah_enterprise.payment.infrastructure.persistence.repository.*;

import java.util.*;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;

@Repository
@RequiredArgsConstructor
public class ReceivableRepositoryAdapter implements ReceivableRepository {

    private final ReceivableJpaRepository jpa;
    private final PaymentMapper mapper;

    @Override
    public Receivable save(Receivable receivable) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(receivable)));
    }

    @Override
    public Optional<Receivable> findBySaleId(UUID saleId) {
        return jpa.findById(saleId).map(mapper::toDomain);
    }

    @Override
    public Optional<Receivable> findBySaleIdForUpdate(UUID saleId) {
        return jpa.findForUpdate(saleId).map(mapper::toDomain);
    }

    @Override
    public List<Receivable> findOpenByCustomerForUpdate(UUID customerId) {
        return jpa.findOpenForUpdate(customerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public PageSlice<Receivable> searchOpen(UUID customerId, UUID locationId, int page, int size) {
        Specification<ReceivableJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();
            all.add(cb.greaterThan(root.<Long>get("totalAmount"), root.<Long>get("allocatedAmount")));
            if (customerId != null) all.add(cb.equal(root.get("customerId"), customerId));
            if (locationId != null) all.add(cb.equal(root.get("locationId"), locationId));
            return cb.and(all.toArray(new Predicate[0]));
        };
        Page<ReceivableJpaEntity> found = jpa.findAll(spec, PageRequest.of(page, size,
                Sort.by(Sort.Order.asc("createdAt"))));
        return new PageSlice<>(found.getContent().stream().map(mapper::toDomain).toList(), found.getTotalElements());
    }

    @Override
    public Money outstandingOfCustomer(UUID customerId) {
        return Money.of(jpa.sumOutstanding(customerId));
    }
}
