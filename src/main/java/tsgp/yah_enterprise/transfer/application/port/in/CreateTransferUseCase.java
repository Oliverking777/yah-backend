package tsgp.yah_enterprise.transfer.application.port.in;

import tsgp.yah_enterprise.transfer.application.dto.CreateTransferCommand;
import tsgp.yah_enterprise.transfer.application.dto.TransferResult;

public interface CreateTransferUseCase {
    /**
     * Creates and sends a transfer: it starts SUBMITTED (in transit). One transaction: the transfer, its event and
     * one TRANSFER_OUT movement per line at the source, taken in productId order so two transfers never wait on
     * each other. The same transferId by the same user returns the existing transfer without moving stock again (F5).
     * Both locations must be active and the user must be assigned to the source location.
     */
    TransferResult createTransfer(CreateTransferCommand command);
}
