package tsgp.yah_enterprise.purchasing.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * A purchase bought and received on the spot: created and confirmed in one step.
 * purchaseId is an optional client id: sending the same purchaseId again returns what was already recorded, so a
 * retry never creates a second purchase.
 */
public record DirectPurchaseCommand(UUID purchaseId, UUID supplierId, UUID destinationLocationId, String deliveryNote,
                                    String note, List<DirectPurchaseLineCommand> items, PurchaseActor actor) { }
