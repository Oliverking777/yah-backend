package tsgp.yah_enterprise.reports.domain.exception;

/** Base of every Reports business-rule failure. */
public abstract class ReportsDomainException extends RuntimeException {
    protected ReportsDomainException(String message) {
        super(message);
    }
}
