package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.DuplicateProductInInitializationException;
import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockInitializationException;
import tsgp.yah_enterprise.inventory.domain.valueobject.InitializationNumber;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate root: a bulk stock initialization at one location.
 * <p>
 * The owner selects a location, adds multiple products with their units and counted quantities,
 * and submits once. The system creates stock balances, records stock movements and an audit entry —
 * all atomically. The initialization is COMPLETED upon submission; there is no approval step.
 * <p>
 * Each product may be initialized at a given location only once (enforced by a unique constraint
 * on stock_initialization_items (location_id, product_id) and checked in the service before posting).
 */
public class StockInitialization {

    private final UUID initializationId;
    private final InitializationNumber initializationNumber;
    private final UUID locationId;
    private StockInitializationStatus status;
    private final UUID initializedBy;
    private final Instant createdAt;
    private Instant completedAt;
    private final List<StockInitializationItem> items;

    private StockInitialization(UUID initializationId, InitializationNumber initializationNumber,
                                 UUID locationId, StockInitializationStatus status, UUID initializedBy,
                                 Instant createdAt, Instant completedAt,
                                 List<StockInitializationItem> items) {
        this.initializationId = Objects.requireNonNull(initializationId, "initializationId must not be null");
        this.initializationNumber = Objects.requireNonNull(initializationNumber, "initializationNumber must not be null");
        this.locationId = Objects.requireNonNull(locationId, "locationId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.initializedBy = Objects.requireNonNull(initializedBy, "initializedBy must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.completedAt = completedAt;
        this.items = new ArrayList<>(Objects.requireNonNull(items, "items must not be null"));
    }

    /**
     * Creates and immediately completes a stock initialization.
     * The caller posts the stock movements in the same transaction.
     *
     * @param items the product lines; must not be empty and must not contain duplicate products
     * @throws InvalidStockInitializationException       if the items list is empty
     * @throws DuplicateProductInInitializationException if the same product appears more than once
     */
    public static StockInitialization create(InitializationNumber initializationNumber, UUID locationId,
                                             UUID initializedBy, List<StockInitializationItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) {
            throw new InvalidStockInitializationException("A stock initialization must contain at least one product");
        }
        ensureNoDuplicateProducts(items);
        Instant now = Instant.now();
        return new StockInitialization(UUID.randomUUID(), initializationNumber, locationId,
                StockInitializationStatus.COMPLETED, initializedBy, now, now, items);
    }

    public static StockInitialization reconstitute(UUID initializationId, InitializationNumber initializationNumber,
                                                   UUID locationId, StockInitializationStatus status,
                                                   UUID initializedBy, Instant createdAt,
                                                   Instant completedAt,
                                                   List<StockInitializationItem> items) {
        return new StockInitialization(initializationId, initializationNumber, locationId, status,
                initializedBy, createdAt, completedAt, items);
    }

    private static void ensureNoDuplicateProducts(List<StockInitializationItem> items) {
        Set<UUID> seen = new HashSet<>();
        for (StockInitializationItem item : items) {
            if (!seen.add(item.getProductId())) {
                throw new DuplicateProductInInitializationException(item.getProductId());
            }
        }
    }

    public UUID getInitializationId() { return initializationId; }
    public InitializationNumber getInitializationNumber() { return initializationNumber; }
    public UUID getLocationId() { return locationId; }
    public StockInitializationStatus getStatus() { return status; }
    public UUID getInitializedBy() { return initializedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }

    /** The items, defensively copied. */
    public List<StockInitializationItem> getItems() { return List.copyOf(items); }

    @Override public boolean equals(Object o) {
        return o instanceof StockInitialization other && initializationId.equals(other.initializationId);
    }
    @Override public int hashCode() { return initializationId.hashCode(); }
}
