package tsgp.yah_enterprise.transfer.presentation.dto;

import tsgp.yah_enterprise.transfer.application.dto.TransferItemResult;

import java.util.UUID;

public record TransferItemResponse(
        UUID transferItemId, UUID productId, String productCode, String productName,
        UUID unitId, String unitName, int conversionFactor,
        int quantity, Integer receivedQuantity, Integer damagedQuantity, Integer missingQuantity,
        String discrepancyReason, long baseQuantity) {

    public static TransferItemResponse from(TransferItemResult r) {
        return new TransferItemResponse(r.transferItemId(), r.productId(), r.productCode(), r.productName(),
                r.unitId(), r.unitName(), r.conversionFactor(), r.quantity(), r.receivedQuantity(),
                r.damagedQuantity(), r.missingQuantity(), r.discrepancyReason(), r.baseQuantity());
    }
}