package tsgp.yah_enterprise.audit.domain.exception;

/** 400: the entry is not valid. */
public class InvalidAuditEntryException extends AuditDomainException {
    public InvalidAuditEntryException(String message) {
        super(message);
    }
}
