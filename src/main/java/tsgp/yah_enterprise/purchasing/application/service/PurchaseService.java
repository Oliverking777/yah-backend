package tsgp.yah_enterprise.purchasing.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.purchasing.application.dto.*;
import tsgp.yah_enterprise.purchasing.application.port.in.*;
import tsgp.yah_enterprise.purchasing.domain.exception.*;
import tsgp.yah_enterprise.purchasing.domain.model.*;
import tsgp.yah_enterprise.purchasing.domain.port.*;
import tsgp.yah_enterprise.purchasing.domain.port.PurchaseAuditPort.Change;
import tsgp.yah_enterprise.purchasing.domain.repository.*;
import tsgp.yah_enterprise.purchasing.domain.valueobject.PurchaseNumber;

import java.util.*;

/**
 * The life of a purchase: order (SUBMITTED), change it while nothing has arrived, confirm the delivery (CONFIRMED)
 * or cancel it (CANCELLED); and the reads. Confirming is the only moment stock increases.
 */
@Service
@Transactional
public class PurchaseService implements CreatePurchaseUseCase, UpdatePurchaseUseCase, AddPurchaseItemUseCase,
        ChangePurchaseItemUseCase, RemovePurchaseItemUseCase, ConfirmPurchaseUseCase, CancelPurchaseUseCase,
        GetPurchaseUseCase, ListPurchasesUseCase, ListSupplierProductsUseCase {

    private final PurchaseRepository purchases;
    private final PurchaseNumberSequence numberSequence;
    private final PurchaseProductCatalogPort products;
    private final PurchaseSupplierDirectoryPort suppliers;
    private final PurchaseLocationDirectoryPort locations;
    private final PurchaseStockPort stock;
    private final PurchaseSupport support;

    public PurchaseService(PurchaseRepository purchases, PurchaseNumberSequence numberSequence,
                           PurchaseSupplierDirectoryPort suppliers, PurchaseProductCatalogPort products,
                           PurchaseLocationDirectoryPort locations, PurchaseStockPort stock, PurchaseAuditPort audit) {
        this.purchases = purchases;
        this.numberSequence = numberSequence;
        this.products = products;
        this.suppliers = suppliers;
        this.locations = locations;
        this.stock = stock;
        this.support = new PurchaseSupport(suppliers, products, locations, audit);
    }

    // ------------------------------------------------------------------ order and change

    @Override
    public PurchaseResult createPurchase(CreatePurchaseCommand c) {
        if (c == null) {
            throw new InvalidPurchaseException("The purchase is required");
        }
        PurchaseActor actor = c.actor();
        PurchaseSupport.requireActor(actor);
        if (c.purchaseId() != null) {
            Optional<Purchase> existing = purchases.findById(c.purchaseId());
            if (existing.isPresent()) {
                if (!actor.userId().equals(existing.get().getCreatedBy())) {
                    throw new PurchaseIdAlreadyUsedException("Purchase id " + c.purchaseId() + " is already used");
                }
                return support.toResult(existing.get());
            }
        }
        if (c.supplierId() == null || c.destinationLocationId() == null) {
            throw new InvalidPurchaseException("Supplier and destination location are required");
        }
        support.requireAtLocation(actor, c.destinationLocationId());
        support.requireUsableLocation(c.destinationLocationId());
        support.requireUsableSupplier(c.supplierId());
        List<PurchaseItem> lines = new ArrayList<>();
        if (c.items() != null) {
            c.items().forEach(l -> lines.add(support.buildItem(l)));
        }
        Purchase purchase = Purchase.submit(c.purchaseId(), PurchaseNumber.fromSequence(numberSequence.next()),
                c.supplierId(), c.destinationLocationId(), c.expectedDeliveryDate(), c.note(), actor.userId(), lines);
        Purchase saved = purchases.save(purchase);
        support.audit(actor.userId(), "PURCHASE.SUBMITTED", saved.getPurchaseId(), saved.getDestinationLocationId(),
                null, List.of(new Change("purchaseStatus", null, "SUBMITTED"),
                        new Change("totalAmount", null, String.valueOf(saved.getTotalAmount().amount()))));
        return support.toResult(saved);
    }

    @Override
    public PurchaseResult updatePurchase(UpdatePurchaseCommand c) {
        Purchase p = lock(c.purchaseId());
        support.requireAtLocation(c.actor(), p.getDestinationLocationId());
        if (c.supplierId() != null) {
            support.requireUsableSupplier(c.supplierId());
        }
        if (c.destinationLocationId() != null) {
            support.requireAtLocation(c.actor(), c.destinationLocationId());
            support.requireUsableLocation(c.destinationLocationId());
        }
        p.changeDetails(c.supplierId(), c.destinationLocationId(), c.expectedDeliveryDate(), c.note());
        return support.toResult(purchases.save(p));
    }

    @Override
    public PurchaseResult addPurchaseItem(AddPurchaseItemCommand c) {
        Purchase p = lock(c.purchaseId());
        support.requireAtLocation(c.actor(), p.getDestinationLocationId());
        p.addItem(support.buildItem(c.line()));
        return support.toResult(purchases.save(p));
    }

    @Override
    public PurchaseResult changePurchaseItem(ChangePurchaseItemCommand c) {
        Purchase p = lock(c.purchaseId());
        support.requireAtLocation(c.actor(), p.getDestinationLocationId());
        p.changeItem(c.purchaseItemId(), c.quantity(), c.unitPrice());
        return support.toResult(purchases.save(p));
    }

    @Override
    public PurchaseResult removePurchaseItem(RemovePurchaseItemCommand c) {
        Purchase p = lock(c.purchaseId());
        support.requireAtLocation(c.actor(), p.getDestinationLocationId());
        p.removeItem(c.purchaseItemId());
        return support.toResult(purchases.save(p));
    }

    // ------------------------------------------------------------------ confirm and cancel

    @Override
    public PurchaseResult confirmPurchase(ConfirmPurchaseCommand c) {
        if (c == null) {
            throw new InvalidPurchaseException("The delivery is required");
        }
        PurchaseActor actor = c.actor();
        PurchaseSupport.requireActor(actor);
        // the row lock makes a second confirmation wait here, then find the purchase CONFIRMED
        Purchase p = lock(c.purchaseId());
        support.requireAtLocation(actor, p.getDestinationLocationId());
        List<ReceiptLine> lines = new ArrayList<>();
        for (DeliveryLineCommand l : c.items() == null ? List.<DeliveryLineCommand>of() : c.items()) {
            if (l == null) {
                throw new InvalidReceiptQuantityException("A delivery line cannot be empty");
            }
            lines.add(new ReceiptLine(l.purchaseItemId(), l.accepted(), l.rejected(), l.rejectionType(), l.reason()));
        }
        p.confirm(actor.userId(), c.deliveryNote(), lines);

        // one movement per product (lines of the same product in two units are added together), in line order
        Map<UUID, Long> baseByProduct = new LinkedHashMap<>();
        p.getItems().stream().sorted(Comparator.comparing(PurchaseItem::getPurchaseItemId))
                .filter(i -> i.acceptedBaseQuantity() > 0)
                .forEach(i -> baseByProduct.merge(i.getProductId(), i.acceptedBaseQuantity(), Long::sum));
        baseByProduct.forEach((productId, base) -> stock.receive(productId, p.getDestinationLocationId(), base,
                p.getPurchaseId(), actor.userId(), p.getConfirmedAt()));

        Purchase saved = purchases.save(p);
        List<Change> changes = new ArrayList<>();
        changes.add(new Change("purchaseStatus", "SUBMITTED", "CONFIRMED"));
        if (saved.hasDiscrepancy()) {
            changes.add(new Change("hasDiscrepancy", "false", "true"));
        }
        support.audit(actor.userId(), "PURCHASE.CONFIRMED", saved.getPurchaseId(), saved.getDestinationLocationId(),
                null, changes);
        return support.toResult(saved);
    }

    @Override
    public PurchaseResult cancelPurchase(PurchaseReasonCommand c) {
        Purchase p = lock(c.purchaseId());
        support.requireAtLocation(c.actor(), p.getDestinationLocationId());
        p.cancel(c.actor().userId(), c.reason());
        Purchase saved = purchases.save(p);
        support.audit(c.actor().userId(), "PURCHASE.CANCELLED", p.getPurchaseId(), p.getDestinationLocationId(),
                p.getCancelReason(), List.of(new Change("purchaseStatus", "SUBMITTED", "CANCELLED")));
        return support.toResult(saved);
    }

    // ------------------------------------------------------------------ reads

    @Override
    @Transactional(readOnly = true)
    public PurchaseResult getPurchase(UUID purchaseId, PurchaseActor actor) {
        Purchase p = purchases.findById(purchaseId).orElseThrow(() -> notFound(purchaseId));
        support.requireAtLocation(actor, p.getDestinationLocationId());
        return support.toResult(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchasePageResult listPurchases(PurchasePageRequest r) {
        UUID scope = support.resolveScope(r.actor(), r.locationId());
        PagedPurchases found = purchases.findAll(r.page(), r.size(), r.supplierId(), scope, r.status(),
                r.hasDiscrepancy(), r.from(), r.to());
        Set<UUID> supplierIds = new HashSet<>();
        Set<UUID> productIds = new HashSet<>();
        found.items().forEach(p -> {
            supplierIds.add(p.getSupplierId());
            p.getItems().forEach(i -> productIds.add(i.getProductId()));
        });
        Map<UUID, String> supplierNames = new HashMap<>();
        suppliers.findAllByIds(supplierIds).forEach(s -> supplierNames.put(s.supplierId(), s.supplierName()));
        Map<UUID, String> locationNames = new HashMap<>();
        Map<UUID, String> productNames = new HashMap<>();
        productIds.forEach(id -> products.findProduct(id).ifPresent(x -> productNames.put(id, x.productName())));
        List<PurchaseResult> content = found.items().stream().map(p -> {
            String locationName = locationNames.computeIfAbsent(p.getDestinationLocationId(), id -> locations
                    .findLocation(id).map(PurchaseLocationDirectoryPort.PurchaseLocation::locationName).orElse(null));
            return PurchaseSupport.build(p, supplierNames.get(p.getSupplierId()), locationName, productNames);
        }).toList();
        return PurchasePageResult.of(content, r.page(), r.size(), found.total());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierProductPageResult listSupplierProducts(SupplierProductsRequest r) {
        PagedIds ids = purchases.findDistinctProductIdsBySupplier(r.supplierId(), r.page(), r.size());
        List<SupplierProductResult> content = ids.items().stream().map(id -> new SupplierProductResult(id,
                products.findProduct(id).map(PurchaseProductCatalogPort.PurchaseProduct::productName)
                        .orElse(null))).toList();
        return SupplierProductPageResult.of(content, r.page(), r.size(), ids.total());
    }

    private Purchase lock(UUID purchaseId) {
        return purchases.findByIdForUpdate(purchaseId).orElseThrow(() -> notFound(purchaseId));
    }

    private static PurchaseNotFoundException notFound(UUID purchaseId) {
        return new PurchaseNotFoundException("Purchase " + purchaseId + " was not found");
    }
}
