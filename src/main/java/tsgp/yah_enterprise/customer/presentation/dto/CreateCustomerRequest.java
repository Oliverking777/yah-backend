package tsgp.yah_enterprise.customer.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.customer.application.dto.CreateCustomerCommand;
import tsgp.yah_enterprise.customer.domain.model.CustomerType;

@Schema(description = "Data needed to create a customer")
public record CreateCustomerRequest(

        @Schema(description = "Full name", example = "ABC Trading", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Customer name is required")
        @Size(max = 150, message = "Customer name must not exceed 150 characters")
        String customerName,

        @Schema(description = "Phone number (digits, optional leading +, 8-15 digits)", example = "+237600000001")
        String phoneNumber,

        @Schema(description = "Email address", example = "abc@trading.cm")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Schema(description = "Physical address", example = "Rue de la Paix, Douala")
        @Size(max = 300, message = "Address must not exceed 300 characters")
        String address,

        @Schema(description = "INDIVIDUAL or BUSINESS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Customer type is required")
        CustomerType customerType
) {
    public CreateCustomerCommand toCommand() {
        return new CreateCustomerCommand(customerName, phoneNumber, email, address, customerType);
    }
}
