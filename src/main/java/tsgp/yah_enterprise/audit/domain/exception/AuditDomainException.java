package tsgp.yah_enterprise.audit.domain.exception;

/** Base of every business-rule error of the Audit module. */
public abstract class AuditDomainException extends RuntimeException {
    protected AuditDomainException(String message) {
        super(message);
    }
}
