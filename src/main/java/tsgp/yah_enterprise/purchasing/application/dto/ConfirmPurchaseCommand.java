package tsgp.yah_enterprise.purchasing.application.dto;

import java.util.List;
import java.util.UUID;

/** The delivery of a SUBMITTED purchase: one line per purchase line. deliveryNote is the supplier's note number. */
public record ConfirmPurchaseCommand(UUID purchaseId, String deliveryNote, List<DeliveryLineCommand> items,
                                     PurchaseActor actor) { }
