package tsgp.yah_enterprise.purchasing.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.purchasing.domain.model.Purchase;
import tsgp.yah_enterprise.purchasing.domain.model.PurchaseItem;
import tsgp.yah_enterprise.purchasing.domain.valueobject.PurchaseNumber;
import tsgp.yah_enterprise.purchasing.infrastructure.persistence.entity.PurchaseItemJpaEntity;
import tsgp.yah_enterprise.purchasing.infrastructure.persistence.entity.PurchaseJpaEntity;

import java.util.Comparator;
import java.util.List;

@Component
public class PurchasingMapper {

    public PurchaseJpaEntity toEntity(Purchase p) {
        PurchaseJpaEntity e = new PurchaseJpaEntity();
        e.setPurchaseId(p.getPurchaseId());
        e.setPurchaseNumber(p.getPurchaseNumber().value());
        e.setSupplierId(p.getSupplierId());
        e.setDestinationLocationId(p.getDestinationLocationId());
        e.setPurchaseStatus(p.getPurchaseStatus());
        e.setDiscrepancy(p.hasDiscrepancy());
        e.setTotalAmount(p.getTotalAmount().amount());
        e.setExpectedDeliveryDate(p.getExpectedDeliveryDate());
        e.setNote(p.getNote());
        e.setDeliveryNote(p.getDeliveryNote());
        e.setCreatedBy(p.getCreatedBy());
        e.setConfirmedBy(p.getConfirmedBy());
        e.setCancelledBy(p.getCancelledBy());
        e.setCancelReason(p.getCancelReason());
        e.setCreatedAt(p.getCreatedAt());
        e.setConfirmedAt(p.getConfirmedAt());
        e.setCancelledAt(p.getCancelledAt());
        e.setUpdatedAt(p.getUpdatedAt());
        e.setVersion(p.getVersion() + 1);
        for (PurchaseItem i : p.getItems()) {
            PurchaseItemJpaEntity ie = new PurchaseItemJpaEntity();
            ie.setPurchaseItemId(i.getPurchaseItemId());
            ie.setPurchase(e);
            ie.setProductId(i.getProductId());
            ie.setUnitId(i.getUnitId());
            ie.setConversionFactor(i.getConversionFactor());
            ie.setOrderedQuantity(i.getOrderedQuantity());
            ie.setUnitPurchasePrice(i.getUnitPurchasePrice().amount());
            ie.setLineTotal(i.lineTotal().amount());
            ie.setAcceptedQuantity(i.getAcceptedQuantity());
            ie.setRejectedQuantity(i.getRejectedQuantity());
            ie.setRejectionType(i.getRejectionType());
            ie.setDiscrepancyReason(i.getDiscrepancyReason());
            e.getItems().add(ie);
        }
        return e;
    }

    public Purchase toDomain(PurchaseJpaEntity e) {
        List<PurchaseItem> items = e.getItems().stream()
                .sorted(Comparator.comparing(PurchaseItemJpaEntity::getPurchaseItemId))
                .map(i -> PurchaseItem.reconstitute(i.getPurchaseItemId(), i.getProductId(), i.getUnitId(),
                        i.getConversionFactor(), i.getOrderedQuantity(), i.getUnitPurchasePrice(),
                        i.getAcceptedQuantity(), i.getRejectedQuantity(), i.getRejectionType(),
                        i.getDiscrepancyReason()))
                .toList();
        return Purchase.reconstitute(e.getPurchaseId(), PurchaseNumber.of(e.getPurchaseNumber()), e.getSupplierId(),
                e.getDestinationLocationId(), e.getPurchaseStatus(), e.isDiscrepancy(), items,
                e.getExpectedDeliveryDate(), e.getNote(), e.getDeliveryNote(), e.getCreatedBy(), e.getConfirmedBy(),
                e.getCancelledBy(), e.getCancelReason(), e.getCreatedAt(), e.getConfirmedAt(), e.getCancelledAt(),
                e.getUpdatedAt(), e.getVersion());
    }
}
