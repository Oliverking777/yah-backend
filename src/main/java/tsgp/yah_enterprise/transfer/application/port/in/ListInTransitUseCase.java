package tsgp.yah_enterprise.transfer.application.port.in;

import tsgp.yah_enterprise.transfer.application.dto.InTransitResult;
import tsgp.yah_enterprise.transfer.application.dto.TransferActor;

import java.util.UUID;

public interface ListInTransitUseCase {
    InTransitResult listInTransit(UUID locationId, TransferActor actor);
}
