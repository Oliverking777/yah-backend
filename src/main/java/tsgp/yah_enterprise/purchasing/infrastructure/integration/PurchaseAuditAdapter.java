package tsgp.yah_enterprise.purchasing.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.purchasing.domain.port.PurchaseAuditPort;
import tsgp.yah_enterprise.audit.application.dto.AuditChangeCommand;
import tsgp.yah_enterprise.audit.application.dto.RecordAuditEntryCommand;
import tsgp.yah_enterprise.audit.application.port.in.RecordAuditEntryUseCase;
import tsgp.yah_enterprise.audit.domain.model.ActorType;
import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;

import java.time.Instant;
import java.util.UUID;

/** Purchasing writes to the audit log through the Audit module's inbound port only. SUCCESS entries join the caller's transaction. */
@Component
@RequiredArgsConstructor
public class PurchaseAuditAdapter implements PurchaseAuditPort {

    private final RecordAuditEntryUseCase recordAuditEntryUseCase;

    @Override
    public void record(AuditRecord r) {
        recordAuditEntryUseCase.recordAuditEntry(new RecordAuditEntryCommand(r.actorId(), ActorType.USER, null,
                r.action(), r.entityType(), r.entityId(), r.locationId(), AuditOutcome.SUCCESS, r.reason(),
                r.changes().stream().map(c -> new AuditChangeCommand(c.fieldName(), c.oldValue(), c.newValue()))
                        .toList(),
                UUID.randomUUID(), Instant.now()));
    }
}
