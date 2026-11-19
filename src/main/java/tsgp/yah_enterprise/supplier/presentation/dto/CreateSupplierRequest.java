package tsgp.yah_enterprise.supplier.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.supplier.application.dto.CreateSupplierCommand;

@Schema(description = "Data needed to create a supplier")
public record CreateSupplierRequest(

        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        String supplierName,

        @Size(max = 100, message = "Contact person must not exceed 100 characters")
        String contactPerson,

        @Schema(description = "Phone number (digits, optional leading +, 8-15 digits)")
        String phoneNumber,

        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Size(max = 300, message = "Address must not exceed 300 characters")
        String address
) {
    public CreateSupplierCommand toCommand() {
        return new CreateSupplierCommand(supplierName, contactPerson, phoneNumber, email, address);
    }
}
