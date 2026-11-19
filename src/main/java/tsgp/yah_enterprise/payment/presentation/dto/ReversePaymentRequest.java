package tsgp.yah_enterprise.payment.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReversePaymentRequest(
        @NotBlank(message = "Reason is required")
        @Size(min = 5, max = 500, message = "Reason must be 5 to 500 characters") String reason) {
}
