package tsgp.yah_enterprise.transfer.application.port.in;

import tsgp.yah_enterprise.transfer.application.dto.TransferPageRequest;
import tsgp.yah_enterprise.transfer.application.dto.TransferPageResult;

public interface ListTransfersUseCase {
    /** Covers the transfers of a location and the discrepancy review list (hasDiscrepancy = true). */
    TransferPageResult listTransfers(TransferPageRequest request);
}