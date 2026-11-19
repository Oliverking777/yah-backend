package tsgp.yah_enterprise.payment.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectCreditRequest(
        @NotBlank(message = "Note is required")
        @Size(min = 5, max = 500, message = "Note must be 5 to 500 characters") String note) {
}
