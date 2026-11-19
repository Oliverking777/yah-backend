package tsgp.yah_enterprise.inventory.domain.exception;

import java.util.UUID;

/** The same product appears more than once in a single initialization request. */
public class DuplicateProductInInitializationException extends InventoryDomainException {

    private final UUID productId;

    public DuplicateProductInInitializationException(UUID productId) {
        super("Product " + productId + " appears more than once in the initialization");
        this.productId = productId;
    }

    public UUID getProductId() { return productId; }
}
