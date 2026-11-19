package tsgp.yah_enterprise.transfer.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.transfer.domain.model.Transfer;
import tsgp.yah_enterprise.transfer.domain.model.TransferEvent;
import tsgp.yah_enterprise.transfer.domain.model.TransferItem;
import tsgp.yah_enterprise.transfer.domain.valueobject.TransferNumber;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.entity.TransferEventJpaEntity;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.entity.TransferItemJpaEntity;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.entity.TransferJpaEntity;

import java.util.Comparator;
import java.util.List;

@Component
public class TransferMapper {

    public Transfer toDomain(TransferJpaEntity e) {
        // a fixed order, so the lines look the same whether they come from a save or a later read
        List<TransferItem> items = e.getItems().stream()
                .sorted(Comparator.comparing(TransferItemJpaEntity::getProductId))
                .map(i -> TransferItem.reconstitute(i.getTransferItemId(), i.getProductId(), i.getUnitId(),
                        i.getConversionFactor(), i.getQuantity(), i.getReceivedQuantity(), i.getDamagedQuantity(),
                        i.getDiscrepancyReason()))
                .toList();
        return Transfer.reconstitute(e.getTransferId(), TransferNumber.of(e.getTransferNumber()),
                e.getSourceLocationId(), e.getDestinationLocationId(), e.getTransferStatus(), e.isDiscrepancy(), items, e.getNote(),
                e.getCreatedBy(), e.getReceivedBy(), e.getCancelledBy(), e.getCancelReason(), e.getCreatedAt(),
                e.getReceivedAt(), e.getCancelledAt(), e.getUpdatedAt());
    }

    public TransferJpaEntity toEntity(Transfer t) {
        TransferJpaEntity e = new TransferJpaEntity();
        e.setTransferId(t.getTransferId());
        e.setTransferNumber(t.getTransferNumber().value());
        e.setSourceLocationId(t.getSourceLocationId());
        e.setDestinationLocationId(t.getDestinationLocationId());
        e.setTransferStatus(t.getTransferStatus());
        e.setDiscrepancy(t.hasDiscrepancy());
        e.setNote(t.getNote());
        e.setCreatedBy(t.getCreatedBy());
        e.setReceivedBy(t.getReceivedBy());
        e.setCancelledBy(t.getCancelledBy());
        e.setCancelReason(t.getCancelReason());
        e.setCreatedAt(t.getCreatedAt());
        e.setReceivedAt(t.getReceivedAt());
        e.setCancelledAt(t.getCancelledAt());
        e.setUpdatedAt(t.getUpdatedAt());
        for (TransferItem item : t.getItems()) {
            TransferItemJpaEntity ie = new TransferItemJpaEntity();
            ie.setTransferItemId(item.getTransferItemId());
            ie.setTransfer(e);
            ie.setProductId(item.getProductId());
            ie.setUnitId(item.getUnitId());
            ie.setConversionFactor(item.getConversionFactor());
            ie.setQuantity(item.getQuantity());
            ie.setReceivedQuantity(item.getReceivedQuantity());
            ie.setDamagedQuantity(item.getDamagedQuantity());
            ie.setDiscrepancyReason(item.getDiscrepancyReason());
            e.getItems().add(ie);
        }
        return e;
    }

    public TransferEvent toDomain(TransferEventJpaEntity e) {
        return TransferEvent.reconstitute(e.getEventId(), e.getTransferId(), e.getEventType(), e.getFromStatus(),
                e.getToStatus(), e.getPerformedBy(), e.getOccurredAt(), e.getNote());
    }

    public TransferEventJpaEntity toEntity(TransferEvent ev) {
        TransferEventJpaEntity e = new TransferEventJpaEntity();
        e.setEventId(ev.getEventId());
        e.setTransferId(ev.getTransferId());
        e.setEventType(ev.getEventType());
        e.setFromStatus(ev.getFromStatus());
        e.setToStatus(ev.getToStatus());
        e.setPerformedBy(ev.getPerformedBy());
        e.setOccurredAt(ev.getOccurredAt());
        e.setNote(ev.getNote());
        return e;
    }
}