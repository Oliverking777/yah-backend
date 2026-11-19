package tsgp.yah_enterprise.audit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import tsgp.yah_enterprise.audit.domain.model.ActorType;
import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;

import java.time.Instant;
import java.util.UUID;

/** Insert-only: the database refuses UPDATE and DELETE. Every column is therefore not updatable. */
@Entity
@Table(name = "audit_entries")
@Getter @Setter @NoArgsConstructor
public class AuditEntryJpaEntity {

    @Id
    @Column(name = "audit_entry_id", nullable = false, updatable = false)
    private UUID auditEntryId;

    @Column(name = "actor_id", updatable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, updatable = false, length = 10)
    private ActorType actorType;

    @Column(name = "device_id", updatable = false, length = 100)
    private String deviceId;

    @Column(name = "action", nullable = false, updatable = false, length = 80)
    private String action;

    @Column(name = "entity_type", nullable = false, updatable = false, length = 40)
    private String entityType;

    @Column(name = "entity_id", updatable = false)
    private UUID entityId;

    @Column(name = "location_id", updatable = false)
    private UUID locationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, updatable = false, length = 10)
    private AuditOutcome outcome;

    @Column(name = "reason", updatable = false, length = 500)
    private String reason;

    /** JSON text of the changed fields, stored in a JSONB column (cast on write, read back as text). */
    @Column(name = "changes", updatable = false, columnDefinition = "jsonb")
    @ColumnTransformer(read = "changes::text", write = "?::jsonb")
    private String changes;

    @Column(name = "correlation_id", nullable = false, updatable = false)
    private UUID correlationId;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;
}
