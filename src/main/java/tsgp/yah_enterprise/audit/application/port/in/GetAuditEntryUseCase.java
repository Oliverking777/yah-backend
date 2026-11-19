package tsgp.yah_enterprise.audit.application.port.in;

import tsgp.yah_enterprise.audit.application.dto.*;
import java.util.*;

public interface GetAuditEntryUseCase {
    AuditEntryResult getAuditEntry(UUID auditEntryId);
}
