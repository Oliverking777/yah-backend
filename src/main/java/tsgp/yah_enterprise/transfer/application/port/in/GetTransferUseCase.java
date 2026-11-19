package tsgp.yah_enterprise.transfer.application.port.in;

import tsgp.yah_enterprise.transfer.application.dto.TransferActor;
import tsgp.yah_enterprise.transfer.application.dto.TransferResult;

import java.util.UUID;

public interface GetTransferUseCase {
    TransferResult getTransfer(UUID transferId, TransferActor actor);
}
