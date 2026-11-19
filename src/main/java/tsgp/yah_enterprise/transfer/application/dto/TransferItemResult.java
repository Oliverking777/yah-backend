package tsgp.yah_enterprise.transfer.application.dto;

import tsgp.yah_enterprise.transfer.domain.model.TransferItem;
import tsgp.yah_enterprise.transfer.domain.port.TransferProduct;
import tsgp.yah_enterprise.transfer.domain.port.TransferProductUnit;

import java.util.UUID;

/**
 * A line: what was sent and, once received, what arrived. Names are null when the product no longer resolves.
 * receivedQuantity, damagedQuantity and missingQuantity are null until the transfer is received.
 */
public record TransferItemResult(
        UUID transferItemId, UUID productId, String productCode, String productName,
        UUID unitId, String unitName, int conversionFactor,
        int quantity, Integer receivedQuantity, Integer damagedQuantity, Integer missingQuantity,
        String discrepancyReason, long baseQuantity) {

    public static TransferItemResult of(TransferItem i, TransferProduct product) {
        String unitName = product == null ? null
                : product.unit(i.getUnitId()).map(TransferProductUnit::unitName).orElse(null);
        return new TransferItemResult(i.getTransferItemId(), i.getProductId(),
                product == null ? null : product.productCode(), product == null ? null : product.productName(),
                i.getUnitId(), unitName, i.getConversionFactor(),
                i.getQuantity(), i.getReceivedQuantity(), i.getDamagedQuantity(), i.missingQuantity(),
                i.getDiscrepancyReason(), i.sentBaseQuantity());
    }
}
