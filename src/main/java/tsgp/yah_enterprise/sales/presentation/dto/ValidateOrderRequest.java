package tsgp.yah_enterprise.sales.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Optional discounts, one per line, in whole FCFA. Cannot be more than the line amount.")
public record ValidateOrderRequest(
        @Size(max = 100, message = "At most 100 discounts") List<@Valid @NotNull(message = "A discount cannot be empty") DiscountRequest> discounts) {
}
