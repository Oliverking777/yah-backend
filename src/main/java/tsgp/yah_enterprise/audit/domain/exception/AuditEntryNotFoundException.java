package tsgp.yah_enterprise.audit.domain.exception;

/** 404: the audit entry does not exist. */
public class AuditEntryNotFoundException extends AuditDomainException {
    public AuditEntryNotFoundException(String message) {
        super(message);
    }
}
