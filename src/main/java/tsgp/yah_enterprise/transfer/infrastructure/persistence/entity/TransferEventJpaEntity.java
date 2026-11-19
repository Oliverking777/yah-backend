package tsgp.yah_enterprise.transfer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.transfer.domain.model.TransferEventType;
import tsgp.yah_enterprise.transfer.domain.model.TransferStatus;

import java.time.Instant;
import java.util.UUID;

/** Append-only: the database refuses UPDATE and DELETE on this table (trigger). */
@Entity
@Table(name = "transfer_events")
@Getter @Setter @NoArgsConstructor
public class TransferEventJpaEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "transfer_id", nullable = false, updatable = false)
    private UUID transferId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false, length = 30)
    private TransferEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", updatable = false, length = 30)
    private TransferStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, updatable = false, length = 30)
    private TransferStatus toStatus;

    @Column(name = "performed_by", nullable = false, updatable = false)
    private UUID performedBy;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "note", updatable = false, length = 500)
    private String note;
}
