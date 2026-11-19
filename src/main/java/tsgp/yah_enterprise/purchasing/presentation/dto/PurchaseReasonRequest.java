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

public record PurchaseReasonRequest(
        @NotBlank(message = "A reason is required") @Size(min = 5, max = 500, message = "The reason must be between 5 and 500 characters") String reason) { }
