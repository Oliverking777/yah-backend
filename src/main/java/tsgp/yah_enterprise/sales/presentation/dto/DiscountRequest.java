package tsgp.yah_enterprise.sales.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tsgp.yah_enterprise.sales.application.dto.DiscountCommand;

import java.util.UUID;

public record DiscountRequest(
        @NotNull(message = "Product is required") UUID productId,
        @NotNull(message = "Unit is required") UUID unitId,
        @Min(value = 0, message = "Discount cannot be negative") long discountAmount) {

    public DiscountCommand toCommand() {
        return new DiscountCommand(productId, unitId, discountAmount);
    }
}
