package tsgp.yah_enterprise.inventory.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import tsgp.yah_enterprise.inventory.domain.exception.InsufficientStockException;
import tsgp.yah_enterprise.inventory.domain.model.MovementType;
import tsgp.yah_enterprise.inventory.domain.model.ReferenceType;
import tsgp.yah_enterprise.inventory.domain.model.StockLevel;
import tsgp.yah_enterprise.inventory.domain.model.StockMovement;
import tsgp.yah_enterprise.inventory.domain.port.InventoryLocation;
import tsgp.yah_enterprise.inventory.domain.port.InventoryLocationDirectoryPort;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProduct;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProductCatalogPort;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProductUnit;
import tsgp.yah_enterprise.inventory.domain.repository.StockLevelRepository;
import tsgp.yah_enterprise.inventory.domain.repository.StockMovementRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * The one place where a stock level and the ledger change. It has no transaction of its own: it always joins
 * the transaction of the caller, so the movement, the level and the caller's own state change commit or roll
 * back together.
 * <p>
 * Every module that removes stock (transfers, sales, purchases, adjustments) goes through here, so this is also
 * where a shortage is told with names instead of ids.
 */
@Component
class StockLedger {

    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;
    /** Only used to name a shortage. Null in tests that build the ledger by hand: the message then has no names. */
    private final InventoryProductCatalogPort productCatalog;
    private final InventoryLocationDirectoryPort locationDirectory;

    /** Kept for callers that build the ledger by hand (the unit tests). The Spring constructor is the next one. */
    StockLedger(StockLevelRepository stockLevelRepository, StockMovementRepository stockMovementRepository) {
        this(stockLevelRepository, stockMovementRepository, null, null);
    }

    @Autowired
    StockLedger(StockLevelRepository stockLevelRepository, StockMovementRepository stockMovementRepository,
                InventoryProductCatalogPort productCatalog, InventoryLocationDirectoryPort locationDirectory) {
        this.stockLevelRepository = stockLevelRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productCatalog = productCatalog;
        this.locationDirectory = locationDirectory;
    }

    /**
     * Posts one movement.
     *
     * @param signedDelta positive adds stock, negative removes it
     * @return the new movement, or the earlier one when this exact document line was already posted (a retry)
     * @throws InsufficientStockException if the stock would go below zero; its message names the product, the
     *                                    location and the base unit
     */
    StockMovement post(UUID productId, UUID locationId, MovementType movementType, long signedDelta,
                       ReferenceType referenceType, UUID referenceId, UUID performedBy,
                       Instant occurredAt, String note) {
        // Lock first, then look for an earlier posting: a retry racing the original waits here and then finds it.
        StockLevel level = stockLevelRepository.lockOrCreate(productId, locationId);

        Optional<StockMovement> earlier = stockMovementRepository.findByReference(
                referenceType, referenceId, productId, locationId, movementType);
        if (earlier.isPresent()) {
            return earlier.get();
        }

        try {
            level.apply(signedDelta);
        } catch (InsufficientStockException shortage) {
            throw named(shortage, productId, locationId);
        }
        StockMovement movement = StockMovement.record(productId, locationId, movementType, signedDelta,
                level.getQuantity(), referenceType, referenceId, performedBy, occurredAt, note);
        stockLevelRepository.save(level);
        return stockMovementRepository.save(movement);
    }

    /**
     * The shortage told with the variant's full name, the location's name and the base unit. Looked up only when
     * stock runs short. A failed lookup never hides the shortage: the plain message is kept.
     */
    private InsufficientStockException named(InsufficientStockException shortage, UUID productId, UUID locationId) {
        if (productCatalog == null || locationDirectory == null) {
            return shortage;
        }
        try {
            String productName = null;
            String unitName = null;
            var detail = productCatalog.findProductDetail(productId);
            if (detail.isPresent()) {
                productName = detail.get().productName();
                // the base unit is the one worth 1 base unit
                unitName = detail.get().units().stream()
                        .filter(u -> u.conversionFactor() == 1)
                        .map(InventoryProductUnit::unitName)
                        .findFirst().orElse(null);
            } else {
                productName = productCatalog.findProduct(productId).map(InventoryProduct::productName).orElse(null);
            }
            String locationName = locationDirectory.findLocation(locationId)
                    .map(InventoryLocation::locationName).orElse(null);
            return shortage.withNames(productName, locationName, unitName);
        } catch (RuntimeException lookupFailed) {
            return shortage;
        }
    }
}