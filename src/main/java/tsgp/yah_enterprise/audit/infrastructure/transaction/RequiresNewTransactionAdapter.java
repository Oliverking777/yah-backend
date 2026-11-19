package tsgp.yah_enterprise.audit.infrastructure.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tsgp.yah_enterprise.audit.application.service.RequiresNewTransaction;

import java.util.function.Supplier;

/** Runs work in a new transaction, apart from the caller's, so a DENIED or FAILED entry survives a rollback. */
@Component
public class RequiresNewTransactionAdapter implements RequiresNewTransaction {

    private final TransactionTemplate template;

    public RequiresNewTransactionAdapter(PlatformTransactionManager transactionManager) {
        this.template = new TransactionTemplate(transactionManager);
        this.template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public <T> T run(Supplier<T> work) {
        return template.execute(status -> work.get());
    }
}
