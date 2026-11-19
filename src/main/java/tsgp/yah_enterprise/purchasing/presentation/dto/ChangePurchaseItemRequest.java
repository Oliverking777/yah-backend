package tsgp.yah_enterprise.purchasing.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import tsgp.yah_enterprise.purchasing.application.dto.*;
import tsgp.yah_enterprise.purchasing.domain.model.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ChangePurchaseItemRequest(
        @Min(value = 1, message = "Quantity must be at least 1") int quantity,
        @Min(value = 1, message = "The price must be greater than zero") long unitPrice) { }
