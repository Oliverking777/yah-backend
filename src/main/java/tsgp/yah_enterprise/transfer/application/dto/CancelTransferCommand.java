package tsgp.yah_enterprise.transfer.application.dto;

import java.util.UUID;

public record CancelTransferCommand(UUID transferId, String reason, TransferActor actor) { }
