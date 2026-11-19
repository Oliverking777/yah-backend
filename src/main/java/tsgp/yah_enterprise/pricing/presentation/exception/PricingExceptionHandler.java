package tsgp.yah_enterprise.pricing.presentation.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.pricing.domain.exception.ApplicablePriceNotFoundException;
import tsgp.yah_enterprise.pricing.domain.exception.InvalidPriceException;
import tsgp.yah_enterprise.pricing.domain.exception.InvalidQuantityRangeException;
import tsgp.yah_enterprise.pricing.domain.exception.PriceAlreadyInactiveException;
import tsgp.yah_enterprise.pricing.domain.exception.PriceProductNotPriceableException;
import tsgp.yah_enterprise.pricing.domain.exception.PriceRangeOverlapException;
import tsgp.yah_enterprise.pricing.domain.exception.ProductPriceNotFoundException;

/**
 * Turns Pricing domain exceptions into ApiResponse bodies.
 * <p>
 * It only handles Pricing exceptions and only for Pricing controllers, and it runs before the
 * IAM handler. Everything else (validation errors, bad JSON, access denied, unexpected errors) is
 * left to the shared GlobalExceptionHandler, so the response format stays the same.
 */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.pricing.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PricingExceptionHandler {

    @ExceptionHandler({ProductPriceNotFoundException.class, ApplicablePriceNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({PriceRangeOverlapException.class, PriceAlreadyInactiveException.class,
            PriceProductNotPriceableException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({InvalidPriceException.class, InvalidQuantityRangeException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidInput(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, message, request.getRequestURI()));
    }
}
