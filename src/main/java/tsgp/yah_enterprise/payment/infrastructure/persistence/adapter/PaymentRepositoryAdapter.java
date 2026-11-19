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
import org.springframework.dao.DataIntegrityViolationException;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("receivedAt"));

    private final PaymentJpaRepository jpa;
    private final PaymentMapper mapper;

    @Override
    public Payment save(Payment payment) {
        try {
            return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(payment)));
        } catch (DataIntegrityViolationException e) {
            if (String.valueOf(e.getMostSpecificCause().getMessage()).contains("uq_payments_method_reference")) {
                throw new tsgp.yah_enterprise.payment.domain.exception.DuplicatePaymentReferenceException(
                        "Reference " + payment.getReference() + " was already used");
            }
            throw e;
        }
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return jpa.findById(paymentId).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByIdForUpdate(UUID paymentId) {
        return jpa.findForUpdate(paymentId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByMethodAndReference(PaymentMethod method, String reference) {
        return jpa.existsByPaymentMethodAndReference(method, reference);
    }

    @Override
    public PageSlice<Payment> search(UUID customerId, UUID locationId, PaymentStatus status, int page, int size) {
        Specification<PaymentJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();
            if (customerId != null) all.add(cb.equal(root.get("customerId"), customerId));
            if (locationId != null) all.add(cb.equal(root.get("locationId"), locationId));
            if (status != null) all.add(cb.equal(root.get("paymentStatus"), status));
            return cb.and(all.toArray(new Predicate[0]));
        };
        Page<PaymentJpaEntity> found = jpa.findAll(spec, PageRequest.of(page, size, NEWEST_FIRST));
        return new PageSlice<>(found.getContent().stream().map(mapper::toDomain).toList(), found.getTotalElements());
    }
}
