package tsgp.yah_enterprise.payment.domain.repository;

import tsgp.yah_enterprise.payment.domain.model.Receivable;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceivableRepository {

    Receivable save(Receivable receivable);

    Optional<Receivable> findBySaleId(UUID saleId);

    Optional<Receivable> findBySaleIdForUpdate(UUID saleId);

    /** Receivables of a customer that still owe money, oldest first, locked. */
    List<Receivable> findOpenByCustomerForUpdate(UUID customerId);

    /** Debts that still owe money. Filters may be null. */
    PageSlice<Receivable> searchOpen(UUID customerId, UUID locationId, int page, int size);

    Money outstandingOfCustomer(UUID customerId);
}
