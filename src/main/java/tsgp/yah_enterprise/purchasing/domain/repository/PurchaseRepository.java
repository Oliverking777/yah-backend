package tsgp.yah_enterprise.purchasing.domain.repository;

import tsgp.yah_enterprise.purchasing.domain.model.Purchase;
import tsgp.yah_enterprise.purchasing.domain.model.PurchaseStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository {

    /** Saves the purchase with its lines. */
    Purchase save(Purchase purchase);

    Optional<Purchase> findById(UUID purchaseId);

    /** Locks the row, so a confirmation, a cancellation and an edit of the same purchase never run together. */
    Optional<Purchase> findByIdForUpdate(UUID purchaseId);

    Optional<Purchase> findByNumber(String purchaseNumber);

    /**
     * Newest first. Every filter is optional. hasDiscrepancy: true for deliveries with rejected or missing goods,
     * false for the others, null for all. from is inclusive and to exclusive, on the creation (order) time.
     */
    PagedPurchases findAll(int page, int size, UUID supplierId, UUID locationId, PurchaseStatus status,
                           Boolean hasDiscrepancy, Instant from, Instant to);

    PagedIds findDistinctProductIdsBySupplier(UUID supplierId, int page, int size);
}
