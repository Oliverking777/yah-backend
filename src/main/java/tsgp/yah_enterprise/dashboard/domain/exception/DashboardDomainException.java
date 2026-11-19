package tsgp.yah_enterprise.dashboard.domain.exception;

/** Base of every Dashboard business-rule failure. */
public abstract class DashboardDomainException extends RuntimeException {
    protected DashboardDomainException(String message) {
        super(message);
    }
}
