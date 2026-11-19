package tsgp.yah_enterprise.audit.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.audit.domain.model.AuditEntry;
import tsgp.yah_enterprise.audit.domain.valueobject.AuditAction;
import tsgp.yah_enterprise.audit.domain.valueobject.AuditActor;
import tsgp.yah_enterprise.audit.infrastructure.persistence.entity.AuditEntryJpaEntity;

@Component
public class AuditEntryMapper {

    public AuditEntryJpaEntity toEntity(AuditEntry e) {
        AuditEntryJpaEntity j = new AuditEntryJpaEntity();
        j.setAuditEntryId(e.getAuditEntryId());
        j.setActorId(e.getActor().actorId());
        j.setActorType(e.getActor().actorType());
        j.setDeviceId(e.getActor().deviceId());
        j.setAction(e.getAction().value());
        j.setEntityType(e.getEntityType());
        j.setEntityId(e.getEntityId());
        j.setLocationId(e.getLocationId());
        j.setOutcome(e.getOutcome());
        j.setReason(e.getReason());
        j.setChanges(AuditChangesJson.write(e.getChanges()));
        j.setCorrelationId(e.getCorrelationId());
        j.setOccurredAt(e.getOccurredAt());
        j.setRecordedAt(e.getRecordedAt());
        return j;
    }

    public AuditEntry toDomain(AuditEntryJpaEntity j) {
        return AuditEntry.reconstitute(j.getAuditEntryId(),
                new AuditActor(j.getActorId(), j.getActorType(), j.getDeviceId()), AuditAction.of(j.getAction()),
                j.getEntityType(), j.getEntityId(), j.getLocationId(), j.getOutcome(), j.getReason(),
                AuditChangesJson.read(j.getChanges()), j.getCorrelationId(), j.getOccurredAt(), j.getRecordedAt());
    }
}
