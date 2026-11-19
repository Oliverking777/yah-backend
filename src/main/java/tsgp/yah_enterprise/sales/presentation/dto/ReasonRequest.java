package tsgp.yah_enterprise.sales.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Used to reject or cancel an order. */
public record ReasonRequest(
        @NotBlank(message = "Reason is required")
        @Size(min = 5, max = 500, message = "Reason must be 5 to 500 characters") String reason) {
}
