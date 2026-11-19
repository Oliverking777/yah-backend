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

@Repository
@RequiredArgsConstructor
public class CreditApprovalRepositoryAdapter implements CreditApprovalRepository {

    private final CreditApprovalJpaRepository jpa;
    private final PaymentMapper mapper;

    @Override
    public CreditApproval save(CreditApproval approval) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(approval)));
    }

    @Override
    public Optional<CreditApproval> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<CreditApproval> findByIdForUpdate(UUID id) {
        return jpa.findForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public Optional<CreditApproval> findActiveBySale(UUID saleId) {
        return jpa.findBySaleIdAndApprovalStatusIn(saleId,
                List.of(CreditApprovalStatus.REQUESTED, CreditApprovalStatus.APPROVED)).map(mapper::toDomain);
    }

    @Override
    public PageSlice<CreditApproval> search(CreditApprovalStatus status, UUID customerId, int page, int size) {
        Specification<CreditApprovalJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();
            if (status != null) all.add(cb.equal(root.get("approvalStatus"), status));
            if (customerId != null) all.add(cb.equal(root.get("customerId"), customerId));
            return cb.and(all.toArray(new Predicate[0]));
        };
        Page<CreditApprovalJpaEntity> found = jpa.findAll(spec, PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("requestedAt"))));
        return new PageSlice<>(found.getContent().stream().map(mapper::toDomain).toList(), found.getTotalElements());
    }
}
