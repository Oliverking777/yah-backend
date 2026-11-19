package tsgp.yah_enterprise.sales.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.sales.domain.model.Order;
import tsgp.yah_enterprise.sales.domain.model.OrderEvent;
import tsgp.yah_enterprise.sales.domain.model.OrderStatus;
import tsgp.yah_enterprise.sales.domain.repository.*;
import tsgp.yah_enterprise.sales.domain.valueobject.OrderNumber;
import tsgp.yah_enterprise.sales.infrastructure.persistence.entity.OrderEventJpaEntity;
import tsgp.yah_enterprise.sales.infrastructure.persistence.entity.OrderJpaEntity;
import tsgp.yah_enterprise.sales.infrastructure.persistence.mapper.OrderMapper;
import tsgp.yah_enterprise.sales.infrastructure.persistence.repository.OrderEventJpaRepository;
import tsgp.yah_enterprise.sales.infrastructure.persistence.repository.OrderJpaRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"));

    private final OrderJpaRepository jpa;
    private final OrderEventJpaRepository eventJpa;
    private final OrderMapper mapper;
    private final EntityManager em;

    @Override
    public Order save(Order order) {
        OrderJpaEntity saved = jpa.saveAndFlush(mapper.toEntity(order));
        List<OrderEventJpaEntity> events = order.pendingEvents().stream().map(mapper::toEntity).toList();
        if (!events.isEmpty()) {
            eventJpa.saveAllAndFlush(events);
        }
        order.clearPendingEvents();
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpa.findById(orderId).map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findByIdForUpdate(UUID orderId) {
        return jpa.findForUpdate(orderId).map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findByNumber(OrderNumber number) {
        return jpa.findByOrderNumber(number.value()).map(mapper::toDomain);
    }

    @Override
    public PagedOrders findAll(int page, int size, UUID locationId, OrderStatus status, UUID customerId,
                               UUID createdBy, Instant from, Instant to) {
        Specification<OrderJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();
            if (locationId != null) all.add(cb.equal(root.get("locationId"), locationId));
            if (status != null) all.add(cb.equal(root.get("orderStatus"), status));
            if (customerId != null) all.add(cb.equal(root.get("customerId"), customerId));
            if (createdBy != null) all.add(cb.equal(root.get("createdBy"), createdBy));
            if (from != null) all.add(cb.greaterThanOrEqualTo(root.<Instant>get("createdAt"), from));
            if (to != null) all.add(cb.lessThan(root.<Instant>get("createdAt"), to));
            return cb.and(all.toArray(new Predicate[0]));
        };
        Page<OrderJpaEntity> found = jpa.findAll(spec, PageRequest.of(page, size, NEWEST_FIRST));
        return new PagedOrders(found.getContent().stream().map(mapper::toDomain).toList(), found.getTotalElements());
    }

    @Override
    public List<SummaryRow> summarize(Instant from, Instant to, SummaryGroupBy groupBy, UUID locationId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> q = cb.createQuery(Object[].class);
        Root<OrderJpaEntity> r = q.from(OrderJpaEntity.class);
        Expression<?> key = switch (groupBy) {
            case EMPLOYEE -> r.get("createdBy");
            case LOCATION -> r.get("locationId");
            case DAY -> cb.function("date", java.sql.Date.class, r.get("completedAt"));
        };
        List<Predicate> where = new ArrayList<>();
        where.add(cb.equal(r.get("orderStatus"), OrderStatus.COMPLETED));
        where.add(cb.greaterThanOrEqualTo(r.<Instant>get("completedAt"), from));
        where.add(cb.lessThan(r.<Instant>get("completedAt"), to));
        if (locationId != null) where.add(cb.equal(r.get("locationId"), locationId));
        q.multiselect(key, cb.count(r), cb.sum(r.<Long>get("totalAmount")))
                .where(where.toArray(new Predicate[0])).groupBy(key).orderBy(cb.asc(key));
        return em.createQuery(q).getResultList().stream()
                .map(x -> new SummaryRow(String.valueOf(x[0]), ((Number) x[1]).longValue(),
                        x[2] == null ? 0L : ((Number) x[2]).longValue()))
                .toList();
    }

    @Override
    public List<OrderEvent> findEvents(UUID orderId) {
        return eventJpa.findAllByOrderIdOrderByOccurredAtAsc(orderId).stream().map(mapper::toDomain).toList();
    }
}
