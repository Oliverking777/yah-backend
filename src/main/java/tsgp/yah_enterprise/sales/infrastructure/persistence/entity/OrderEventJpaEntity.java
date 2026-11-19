package tsgp.yah_enterprise.sales.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import tsgp.yah_enterprise.sales.domain.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_events")
@Getter @Setter @NoArgsConstructor
public class OrderEventJpaEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false, length = 30)
    private OrderEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", updatable = false, length = 30)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, updatable = false, length = 30)
    private OrderStatus toStatus;

    @Column(name = "performed_by", nullable = false, updatable = false)
    private UUID performedBy;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "note", updatable = false, length = 500)
    private String note;
}
