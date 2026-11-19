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
public class PaymentAllocationRepositoryAdapter implements PaymentAllocationRepository {

    private final PaymentAllocationJpaRepository jpa;
    private final PaymentMapper mapper;

    @Override
    public List<PaymentAllocation> saveAll(List<PaymentAllocation> allocations) {
        return jpa.saveAllAndFlush(allocations.stream().map(mapper::toEntity).toList())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PaymentAllocation> findByPaymentId(UUID paymentId) {
        return jpa.findAllByPaymentIdOrderByCreatedAtAsc(paymentId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PaymentAllocation> findBySaleId(UUID saleId) {
        return jpa.findAllBySaleIdOrderByCreatedAtAsc(saleId).stream().map(mapper::toDomain).toList();
    }
}
