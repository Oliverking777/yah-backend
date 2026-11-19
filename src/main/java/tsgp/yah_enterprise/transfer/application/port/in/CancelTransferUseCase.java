package tsgp.yah_enterprise.transfer.application.port.in;

import tsgp.yah_enterprise.transfer.application.dto.CancelTransferCommand;
import tsgp.yah_enterprise.transfer.application.dto.TransferResult;

public interface CancelTransferUseCase {
    /**
     * SUBMITTED to CANCELLED, by a user of the source or the destination. One transaction: the status change, the
     * event and one TRANSFER_IN movement per line at the source for the full quantity sent (the goods go back).
     */
    TransferResult cancelTransfer(CancelTransferCommand command);
}
