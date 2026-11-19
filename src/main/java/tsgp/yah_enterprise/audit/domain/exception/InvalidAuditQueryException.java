package tsgp.yah_enterprise.audit.domain.exception;

/** 400: the search or paging is not valid (range over 366 days, bad paging). */
public class InvalidAuditQueryException extends AuditDomainException {
    public InvalidAuditQueryException(String message) {
        super(message);
    }
}
