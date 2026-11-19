package tsgp.yah_enterprise.transfer.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Why the transfer is cancelled")
public record CancelTransferRequest(
        @Schema(description = "5 to 500 characters", example = "Wrong destination chosen")
        @NotBlank(message = "A reason is required") @Size(max = 500, message = "Reason must be at most 500 characters") String reason) { }
