package tsgp.yah_enterprise.reports.infrastructure.integration;

import tsgp.yah_enterprise.reports.domain.port.ReportAuditPort;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.audit.application.dto.AuditChangeCommand;
import tsgp.yah_enterprise.audit.application.dto.RecordAuditEntryCommand;
import tsgp.yah_enterprise.audit.application.port.in.RecordAuditEntryUseCase;
import tsgp.yah_enterprise.audit.domain.model.ActorType;
import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;
import tsgp.yah_enterprise.audit.infrastructure.context.AuditCallContext;

import java.time.Instant;
import java.util.UUID;

/** Reports writes to the audit log through the Audit module's inbound port only. */
@Component
public class ReportAuditAdapter implements ReportAuditPort {

    private final RecordAuditEntryUseCase recordAuditEntryUseCase;

    public ReportAuditAdapter(RecordAuditEntryUseCase recordAuditEntryUseCase) {
        this.recordAuditEntryUseCase = recordAuditEntryUseCase;
    }

    @Override
    public void record(AuditRecord r) {
        UUID actor = r.actorId() != null ? r.actorId() : AuditCallContext.currentUserId().orElse(null);
        recordAuditEntryUseCase.recordAuditEntry(new RecordAuditEntryCommand(actor,
                actor == null ? ActorType.SYSTEM : ActorType.USER, AuditCallContext.deviceId(), r.action(),
                r.entityType(), r.entityId(), r.locationId(), AuditOutcome.SUCCESS, r.reason(),
                r.changes().stream().map(c -> new AuditChangeCommand(c.fieldName(), c.oldValue(), c.newValue()))
                        .toList(),
                AuditCallContext.correlationId(), Instant.now()));
    }
}
