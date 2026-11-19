package tsgp.yah_enterprise.audit.application.service;

import java.util.function.Supplier;

/**
 * Runs work in its OWN transaction. A DENIED or FAILED entry must survive even when the business transaction rolls
 * back, so it cannot join it. Implemented in the infrastructure layer.
 */
public interface RequiresNewTransaction {
    <T> T run(Supplier<T> work);
}
