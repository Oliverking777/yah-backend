package tsgp.yah_enterprise.reports.domain.port;

import tsgp.yah_enterprise.reports.domain.model.*;
import tsgp.yah_enterprise.reports.domain.valueobject.*;

import java.util.*;

/** Reports' way to write to the audit log (REPORT.EXPORTED). Implemented in infrastructure/integration. */
public interface ReportAuditPort {

    void record(AuditRecord record);

    record AuditRecord(UUID actorId, String action, String entityType, UUID entityId, UUID locationId,
                       String reason, List<Change> changes) {
        public AuditRecord {
            changes = changes == null ? List.of() : List.copyOf(changes);
        }
    }

    record Change(String fieldName, String oldValue, String newValue) { }
}
