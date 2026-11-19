package tsgp.yah_enterprise.customer.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.customer.application.dto.UpdateCustomerCommand;

@Schema(description = "Fields to update on a customer. Omitted fields are unchanged.")
public record UpdateCustomerRequest(

        @Schema(description = "New name", example = "ABC Trading Ltd")
        @Size(max = 150, message = "Customer name must not exceed 150 characters")
        String customerName,

        @Schema(description = "New phone number", example = "+237600000002")
        String phoneNumber,

        @Schema(description = "New email", example = "info@abctrading.cm")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Schema(description = "New address", example = "Akwa, Douala")
        @Size(max = 300, message = "Address must not exceed 300 characters")
        String address
) {
    public UpdateCustomerCommand toCommand() {
        return new UpdateCustomerCommand(customerName, phoneNumber, email, address);
    }
}
