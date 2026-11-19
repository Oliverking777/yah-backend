package tsgp.yah_enterprise.transfer.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.transfer.application.dto.CreateTransferCommand;
import tsgp.yah_enterprise.transfer.application.dto.TransferActor;

import java.util.List;
import java.util.UUID;

@Schema(description = "A new transfer, sent at once: the goods leave the source stock. No totals or numbers are "
        + "accepted: the server assigns TRF-000001 style numbers.")
public record CreateTransferRequest(
        @Schema(description = "Optional. An offline app sends its own UUID so a retry returns the same transfer.")
        UUID transferId,
        @Schema(description = "Location the goods leave from") @NotNull(message = "Source location is required") UUID sourceLocationId,
        @Schema(description = "Location the goods go to") @NotNull(message = "Destination location is required") UUID destinationLocationId,
        @Schema(description = "Optional note, up to 500 characters") @Size(max = 500, message = "Note must be at most 500 characters") String note,
        @Schema(description = "1 to 100 lines, one per product")
        @NotEmpty(message = "At least one line is required") @Size(max = 100, message = "At most 100 lines") List<@Valid @NotNull(message = "A line cannot be empty") TransferLineRequest> items) {

    public CreateTransferCommand toCommand(TransferActor actor) {
        return new CreateTransferCommand(transferId, sourceLocationId, destinationLocationId, note,
                items.stream().map(TransferLineRequest::toCommand).toList(), actor);
    }
}