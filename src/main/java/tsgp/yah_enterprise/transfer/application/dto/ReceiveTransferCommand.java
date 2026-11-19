package tsgp.yah_enterprise.transfer.application.dto;

import java.util.List;
import java.util.UUID;

public record ReceiveTransferCommand(UUID transferId, List<ReceiveLineCommand> items, TransferActor actor) { }
