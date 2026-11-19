package tsgp.yah_enterprise.purchasing.application.service;

import tsgp.yah_enterprise.purchasing.application.dto.*;
import tsgp.yah_enterprise.purchasing.domain.exception.*;
import tsgp.yah_enterprise.purchasing.domain.model.*;
import tsgp.yah_enterprise.purchasing.domain.port.*;
import tsgp.yah_enterprise.purchasing.domain.port.PurchaseAuditPort.AuditRecord;
import tsgp.yah_enterprise.purchasing.domain.port.PurchaseAuditPort.Change;

import java.util.*;

/** Checks and mapping shared by the purchasing services. */
final class PurchaseSupport {

    static final String PURCHASE = "PURCHASE";

    private final PurchaseSupplierDirectoryPort suppliers;
    private final PurchaseProductCatalogPort products;
    private final PurchaseLocationDirectoryPort locations;
    private final PurchaseAuditPort audit;

    PurchaseSupport(PurchaseSupplierDirectoryPort suppliers, PurchaseProductCatalogPort products,
                    PurchaseLocationDirectoryPort locations, PurchaseAuditPort audit) {
        this.suppliers = suppliers;
        this.products = products;
        this.locations = locations;
        this.audit = audit;
    }

    static void requireActor(PurchaseActor actor) {
        if (actor == null || actor.userId() == null) {
            throw new InvalidPurchaseException("The acting user is required");
        }
    }

    void requireAtLocation(PurchaseActor actor, UUID locationId) {
        requireActor(actor);
        if (!actor.anyLocation() && !locations.isUserAssigned(actor.userId(), locationId)) {
            throw new PurchaseLocationAccessException("You are not assigned to this location");
        }
    }

    UUID resolveScope(PurchaseActor actor, UUID requested) {
        requireActor(actor);
        if (actor.anyLocation()) {
            return requested;
        }
        UUID own = locations.findLocationIdOfUser(actor.userId())
                .orElseThrow(() -> new PurchaseLocationAccessException("You are not assigned to a location"));
        if (requested != null && !requested.equals(own)) {
            throw new PurchaseLocationAccessException("You are not assigned to this location");
        }
        return own;
    }

    void requireUsableSupplier(UUID supplierId) {
        boolean usable = suppliers.findById(supplierId).map(PurchaseSupplierDirectoryPort.PurchaseSupplier::active)
                .orElse(false);
        if (!usable) {
            throw new PurchaseSupplierNotUsableException("Supplier " + supplierId + " does not exist or is inactive");
        }
    }

    void requireUsableLocation(UUID locationId) {
        boolean usable = locations.findLocation(locationId).map(PurchaseLocationDirectoryPort.PurchaseLocation::active)
                .orElse(false);
        if (!usable) {
            throw new PurchaseLocationNotUsableException("Location " + locationId + " does not exist or is inactive");
        }
    }

    /** Checks the product and the unit through the catalog and copies the conversion factor onto the line. */
    PurchaseItem buildItem(PurchaseLineCommand c) {
        if (c == null || c.productId() == null || c.unitId() == null) {
            throw new InvalidPurchaseException("Product and unit are required on every line");
        }
        PurchaseProductCatalogPort.PurchaseProduct product = products.findProduct(c.productId())
                .filter(PurchaseProductCatalogPort.PurchaseProduct::active)
                .orElseThrow(() -> new PurchaseProductNotPurchasableException(
                        "Product " + c.productId() + " does not exist or is inactive"));
        PurchaseProductCatalogPort.PurchaseProductUnit unit = product.units().stream()
                .filter(u -> u.unitId().equals(c.unitId()) && u.active()).findFirst()
                .orElseThrow(() -> new PurchaseProductNotPurchasableException(
                        "Unit " + c.unitId() + " is not an active unit of product " + product.productName()));
        return PurchaseItem.create(c.productId(), c.unitId(), unit.conversionFactor(), c.quantity(), c.unitPrice());
    }

    void audit(UUID actorId, String action, UUID purchaseId, UUID locationId, String reason, List<Change> changes) {
        audit.record(new AuditRecord(actorId, action, PURCHASE, purchaseId, locationId, reason, changes));
    }

    PurchaseResult toResult(Purchase p) {
        String supplierName = suppliers.findById(p.getSupplierId())
                .map(PurchaseSupplierDirectoryPort.PurchaseSupplier::supplierName).orElse(null);
        String locationName = locations.findLocation(p.getDestinationLocationId())
                .map(PurchaseLocationDirectoryPort.PurchaseLocation::locationName).orElse(null);
        Map<UUID, String> productNames = new HashMap<>();
        for (PurchaseItem i : p.getItems()) {
            productNames.computeIfAbsent(i.getProductId(), id -> products.findProduct(id)
                    .map(PurchaseProductCatalogPort.PurchaseProduct::productName).orElse(null));
        }
        return build(p, supplierName, locationName, productNames);
    }

    static PurchaseResult build(Purchase p, String supplierName, String locationName, Map<UUID, String> productNames) {
        List<PurchaseItemResult> items = p.getItems().stream().map(i -> new PurchaseItemResult(i.getPurchaseItemId(),
                i.getProductId(), productNames.get(i.getProductId()), i.getUnitId(), i.getConversionFactor(),
                i.getOrderedQuantity(), i.getUnitPurchasePrice().amount(), i.lineTotal().amount(),
                i.getAcceptedQuantity(), i.getRejectedQuantity(), i.missingQuantity(),
                i.getRejectionType() == null ? null : i.getRejectionType().name(), i.getDiscrepancyReason())).toList();
        return new PurchaseResult(p.getPurchaseId(), p.getPurchaseNumber().value(), p.getSupplierId(), supplierName,
                p.getDestinationLocationId(), locationName, p.getPurchaseStatus().name(), p.hasDiscrepancy(), items,
                p.getTotalAmount().amount(), p.getExpectedDeliveryDate(), p.getNote(), p.getDeliveryNote(),
                p.getCreatedBy(), p.getConfirmedBy(), p.getCancelledBy(), p.getCancelReason(), p.getCreatedAt(),
                p.getConfirmedAt(), p.getCancelledAt(), p.getUpdatedAt());
    }
}
