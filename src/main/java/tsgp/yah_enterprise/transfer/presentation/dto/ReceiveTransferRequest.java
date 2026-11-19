package tsgp.yah_enterprise.transfer.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import tsgp.yah_enterprise.transfer.application.dto.ReceiveTransferCommand;
import tsgp.yah_enterprise.transfer.application.dto.TransferActor;

import java.util.List;
import java.util.UUID;

@Schema(description = "What arrived, line by line. Every product on the transfer needs a line.")
public record ReceiveTransferRequest(
        @NotEmpty(message = "At least one line is required") List<@Valid @NotNull(message = "A line cannot be empty") ReceiveLineRequest> items) {

    public ReceiveTransferCommand toCommand(UUID transferId, TransferActor actor) {
        return new ReceiveTransferCommand(transferId,
                items.stream().map(ReceiveLineRequest::toCommand).toList(), actor);
    }
}