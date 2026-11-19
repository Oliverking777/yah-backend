package tsgp.yah_enterprise.sales.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.sales.domain.model.*;
import tsgp.yah_enterprise.sales.domain.valueobject.Money;
import tsgp.yah_enterprise.sales.domain.valueobject.OrderNumber;
import tsgp.yah_enterprise.sales.domain.valueobject.ReceiptNumber;
import tsgp.yah_enterprise.sales.infrastructure.persistence.entity.*;

import java.util.Comparator;
import java.util.List;

@Component
public class OrderMapper {

    private static Money money(Long v) {
        return v == null ? null : Money.of(v);
    }

    public Order toDomain(OrderJpaEntity e) {
        // a fixed order, so the lines look the same whether they come from a save or a later read
        List<OrderItem> items = e.getItems().stream()
                .sorted(Comparator.comparing(OrderItemJpaEntity::getProductId)
                        .thenComparing(OrderItemJpaEntity::getUnitId))
                .map(i -> OrderItem.reconstitute(i.getOrderItemId(), i.getProductId(), i.getUnitId(),
                        i.getConversionFactor(), i.getQuantity(), money(i.getUnitPrice()), i.getPriceId(),
                        Money.of(i.getDiscountAmount())))
                .toList();
        return Order.reconstitute(e.getOrderId(), OrderNumber.of(e.getOrderNumber()), e.getLocationId(),
                e.getCustomerId(), e.getOrderStatus(), items, e.getNote(), e.getCreatedBy(), e.getValidatedBy(),
                e.getCompletedBy(), e.getCancelledBy(), e.getRejectionReason(), e.getCancelReason(),
                money(e.getAmountPaid()), money(e.getAmountDue()), e.getCreatedAt(), e.getSubmittedAt(),
                e.getValidatedAt(), e.getCompletedAt(), e.getUpdatedAt());
    }

    public OrderJpaEntity toEntity(Order o) {
        OrderJpaEntity e = new OrderJpaEntity();
        e.setOrderId(o.getOrderId());
        e.setOrderNumber(o.getOrderNumber().value());
        e.setLocationId(o.getLocationId());
        e.setCustomerId(o.getCustomerId());
        e.setOrderStatus(o.getOrderStatus());
        e.setSubtotal(o.getSubtotal().amount());
        e.setDiscountTotal(o.getDiscountTotal().amount());
        e.setTotalAmount(o.getTotalAmount().amount());
        e.setAmountPaid(o.getAmountPaid() == null ? null : o.getAmountPaid().amount());
        e.setAmountDue(o.getAmountDue() == null ? null : o.getAmountDue().amount());
        e.setNote(o.getNote());
        e.setCreatedBy(o.getCreatedBy());
        e.setValidatedBy(o.getValidatedBy());
        e.setCompletedBy(o.getCompletedBy());
        e.setCancelledBy(o.getCancelledBy());
        e.setRejectionReason(o.getRejectionReason());
        e.setCancelReason(o.getCancelReason());
        e.setCreatedAt(o.getCreatedAt());
        e.setSubmittedAt(o.getSubmittedAt());
        e.setValidatedAt(o.getValidatedAt());
        e.setCompletedAt(o.getCompletedAt());
        e.setUpdatedAt(o.getUpdatedAt());
        for (OrderItem item : o.getItems()) {
            OrderItemJpaEntity ie = new OrderItemJpaEntity();
            ie.setOrderItemId(item.getOrderItemId());
            ie.setOrder(e);
            ie.setProductId(item.getProductId());
            ie.setUnitId(item.getUnitId());
            ie.setConversionFactor(item.getConversionFactor());
            ie.setQuantity(item.getQuantity());
            ie.setUnitPrice(item.getUnitPrice() == null ? null : item.getUnitPrice().amount());
            ie.setPriceId(item.getPriceId());
            ie.setDiscountAmount(item.getDiscountAmount().amount());
            ie.setLineTotal(item.getLineTotal().amount());
            e.getItems().add(ie);
        }
        return e;
    }

    public OrderEvent toDomain(OrderEventJpaEntity e) {
        return OrderEvent.reconstitute(e.getEventId(), e.getOrderId(), e.getEventType(), e.getFromStatus(),
                e.getToStatus(), e.getPerformedBy(), e.getOccurredAt(), e.getNote());
    }

    public OrderEventJpaEntity toEntity(OrderEvent ev) {
        OrderEventJpaEntity e = new OrderEventJpaEntity();
        e.setEventId(ev.getEventId());
        e.setOrderId(ev.getOrderId());
        e.setEventType(ev.getEventType());
        e.setFromStatus(ev.getFromStatus());
        e.setToStatus(ev.getToStatus());
        e.setPerformedBy(ev.getPerformedBy());
        e.setOccurredAt(ev.getOccurredAt());
        e.setNote(ev.getNote());
        return e;
    }

    public Receipt toDomain(ReceiptJpaEntity e) {
        return Receipt.reconstitute(e.getReceiptId(), ReceiptNumber.of(e.getReceiptNumber()), e.getOrderId(),
                e.getIssuedAt(), e.getIssuedBy());
    }

    public ReceiptJpaEntity toEntity(Receipt r) {
        ReceiptJpaEntity e = new ReceiptJpaEntity();
        e.setReceiptId(r.getReceiptId());
        e.setReceiptNumber(r.getReceiptNumber().value());
        e.setOrderId(r.getOrderId());
        e.setIssuedAt(r.getIssuedAt());
        e.setIssuedBy(r.getIssuedBy());
        return e;
    }
}
