package tsgp.yah_enterprise.transfer.application.port.in;

import tsgp.yah_enterprise.transfer.application.dto.TransferActor;
import tsgp.yah_enterprise.transfer.application.dto.TransferEventResult;

import java.util.List;
import java.util.UUID;

public interface GetTransferHistoryUseCase {
    /** Oldest first. */
    List<TransferEventResult> getTransferHistory(UUID transferId, TransferActor actor);
}
