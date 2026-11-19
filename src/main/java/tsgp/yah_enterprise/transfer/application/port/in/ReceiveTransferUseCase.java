package tsgp.yah_enterprise.transfer.application.port.in;

import tsgp.yah_enterprise.transfer.application.dto.ReceiveTransferCommand;
import tsgp.yah_enterprise.transfer.application.dto.TransferResult;

public interface ReceiveTransferUseCase {
    /**
     * SUBMITTED to RECEIVED. One transaction: the status change, the event and one TRANSFER_IN movement per line
     * for the good quantity. Damaged and missing goods add no stock and set the transfer's hasDiscrepancy flag.
     * Covers RecordTransferDiscrepancy.
     */
    TransferResult receiveTransfer(ReceiveTransferCommand command);
}
