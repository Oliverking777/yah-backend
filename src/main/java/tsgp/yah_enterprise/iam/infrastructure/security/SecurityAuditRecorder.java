package tsgp.yah_enterprise.iam.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.audit.application.dto.AuditChangeCommand;
import tsgp.yah_enterprise.audit.application.dto.RecordAuditEntryCommand;
import tsgp.yah_enterprise.audit.application.port.in.RecordAuditEntryUseCase;
import tsgp.yah_enterprise.audit.domain.model.ActorType;
import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;
import tsgp.yah_enterprise.audit.infrastructure.context.AuditCallContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Writes AUTH.LOGIN_FAILED and AUTH.ACCESS_DENIED. These are DENIED entries, so the Audit module stores them in a
 * transaction of their own: they survive the rollback of the request that was refused. An audit problem is logged and
 * never replaces the 401 or 403 the caller must receive.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityAuditRecorder {

    private final RecordAuditEntryUseCase recordAuditEntryUseCase;

    public void loginFailed(String userName, String reason) {
        List<AuditChangeCommand> changes = userName == null || userName.isBlank()
                ? List.of() : List.of(new AuditChangeCommand("userName", null, userName));
        write(null, "AUTH.LOGIN_FAILED", AuditOutcome.DENIED, reason, changes);
    }

    public void accessDenied(String method, String path) {
        UUID actor = AuditCallContext.currentUserId().orElse(null);
        write(actor, "AUTH.ACCESS_DENIED", AuditOutcome.DENIED, method + " " + path, List.of());
    }

    private void write(UUID actor, String action, AuditOutcome outcome, String reason,
                       List<AuditChangeCommand> changes) {
        try {
            recordAuditEntryUseCase.recordAuditEntry(new RecordAuditEntryCommand(actor,
                    actor == null ? ActorType.SYSTEM : ActorType.USER, AuditCallContext.deviceId(), action, "AUTH",
                    null, null, outcome, reason, changes, AuditCallContext.correlationId(), Instant.now()));
        } catch (RuntimeException e) {
            log.warn("Could not write the audit entry {}: {}", action, e.getMessage());
        }
    }
}
