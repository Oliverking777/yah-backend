package tsgp.yah_enterprise.supplier.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.supplier.application.dto.UpdateSupplierCommand;

@Schema(description = "Fields to update on a supplier. Omitted fields are unchanged.")
public record UpdateSupplierRequest(

        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        String supplierName,

        @Size(max = 100, message = "Contact person must not exceed 100 characters")
        String contactPerson,

        String phoneNumber,

        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Size(max = 300, message = "Address must not exceed 300 characters")
        String address
) {
    public UpdateSupplierCommand toCommand() {
        return new UpdateSupplierCommand(supplierName, contactPerson, phoneNumber, email, address);
    }
}
