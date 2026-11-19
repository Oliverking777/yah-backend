package tsgp.yah_enterprise.audit.domain.exception;

/** 400: more than 10000 rows, narrow the filters. */
public class AuditExportTooLargeException extends AuditDomainException {
    public AuditExportTooLargeException(String message) {
        super(message);
    }
}
