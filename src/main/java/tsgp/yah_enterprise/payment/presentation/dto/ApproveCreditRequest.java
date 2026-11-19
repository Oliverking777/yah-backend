package tsgp.yah_enterprise.payment.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ApproveCreditRequest(
        @Schema(description = "Optional. Defaults to the requested amount; cannot be more.")
        @Min(value = 1, message = "Amount must be at least 1 FCFA") Long approvedAmount,
        @Size(max = 500, message = "Note must be at most 500 characters") String note) {
}
