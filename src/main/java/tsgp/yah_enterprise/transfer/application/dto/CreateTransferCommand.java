package tsgp.yah_enterprise.transfer.application.dto;

import java.util.List;
import java.util.UUID;

/** transferId is optional: an offline app sends its own UUID so a retry is safe (F5). */
public record CreateTransferCommand(UUID transferId, UUID sourceLocationId, UUID destinationLocationId,
                                    String note, List<TransferLineCommand> items, TransferActor actor) { }
