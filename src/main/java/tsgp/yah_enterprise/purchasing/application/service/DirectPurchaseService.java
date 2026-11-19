package tsgp.yah_enterprise.purchasing.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.purchasing.application.dto.*;
import tsgp.yah_enterprise.purchasing.application.port.in.*;
import tsgp.yah_enterprise.purchasing.domain.exception.InvalidPurchaseException;
import tsgp.yah_enterprise.purchasing.domain.repository.PurchaseRepository;

import java.util.*;

/**
 * Direct purchase: goods bought and received on the spot, in one step instead of two (create, then confirm).
 * <p>
 * It only composes the existing use cases, so every rule they enforce still applies (location access, active
 * supplier and location, purchasable product and unit, rejection type and reason, audit, stock). Both join this
 * transaction: if either fails, nothing is saved. What was kept is both ordered and accepted, so the purchase
 * is CONFIRMED with a discrepancy only when goods were refused at the door.
 */
@Service
@Transactional
public class DirectPurchaseService implements RecordDirectPurchaseUseCase {

    private final CreatePurchaseUseCase createPurchase;
    private final ConfirmPurchaseUseCase confirmPurchase;
    private final PurchaseRepository purchases;

    public DirectPurchaseService(CreatePurchaseUseCase createPurchase, ConfirmPurchaseUseCase confirmPurchase,
                                 PurchaseRepository purchases) {
        this.createPurchase = createPurchase;
        this.confirmPurchase = confirmPurchase;
        this.purchases = purchases;
    }

    @Override
    public PurchaseResult recordDirectPurchase(DirectPurchaseCommand c) {
        if (c == null) {
            throw new InvalidPurchaseException("The purchase is required");
        }
        PurchaseSupport.requireActor(c.actor());

        // A retry with the same purchaseId returns what was already recorded (create and confirm happen together).
        if (c.purchaseId() != null && purchases.findById(c.purchaseId()).isPresent()) {
            // createPurchase answers the same way for a retry, and refuses an id used by someone else
            return createPurchase.createPurchase(new CreatePurchaseCommand(c.purchaseId(), c.supplierId(),
                    c.destinationLocationId(), null, c.note(), List.of(), c.actor()));
        }
        if (c.items() == null || c.items().isEmpty()) {
            throw new InvalidPurchaseException("A direct purchase needs at least one line");
        }

        // 1. The purchase, with what was kept as the ordered quantity.
        List<PurchaseLineCommand> lines = new ArrayList<>();
        for (DirectPurchaseLineCommand l : c.items()) {
            if (l == null) {
                throw new InvalidPurchaseException("Every line is required");
            }
            if (l.quantityReceived() < 1) {
                throw new InvalidPurchaseException("Every line needs at least 1 received; leave out what was all refused");
            }
            if (l.quantityRejected() < 0) {
                throw new InvalidPurchaseException("The rejected quantity cannot be negative");
            }
            lines.add(new PurchaseLineCommand(l.productId(), l.unitId(), l.quantityReceived(), l.unitPrice()));
        }
        PurchaseResult created = createPurchase.createPurchase(new CreatePurchaseCommand(c.purchaseId(),
                c.supplierId(), c.destinationLocationId(), null, c.note(), lines, c.actor()));

        // 2. The delivery, line by line, matched to the purchase lines by product and unit.
        Map<String, UUID> lineIdByProductUnit = new HashMap<>();
        created.items().forEach(i -> lineIdByProductUnit.put(key(i.productId(), i.unitId()), i.purchaseItemId()));
        List<DeliveryLineCommand> delivery = new ArrayList<>();
        for (DirectPurchaseLineCommand l : c.items()) {
            boolean rejected = l.quantityRejected() > 0;
            delivery.add(new DeliveryLineCommand(lineIdByProductUnit.get(key(l.productId(), l.unitId())),
                    l.quantityReceived(), l.quantityRejected(), rejected ? l.rejectionType() : null,
                    rejected ? l.rejectionReason() : null));
        }
        return confirmPurchase.confirmPurchase(new ConfirmPurchaseCommand(created.purchaseId(), c.deliveryNote(),
                delivery, c.actor()));
    }

    private static String key(UUID productId, UUID unitId) {
        return productId + ":" + unitId;
    }
}
