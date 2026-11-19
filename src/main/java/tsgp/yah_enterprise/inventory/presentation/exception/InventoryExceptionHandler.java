package tsgp.yah_enterprise.inventory.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.inventory.domain.exception.*;

/** Maps the Inventory exceptions to HTTP statuses. Anything else falls through to the global handler. */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.inventory.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InventoryExceptionHandler {

    @ExceptionHandler({StockAdjustmentNotFoundException.class, StockInitializationNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InsufficientStockException.class,
            StockAlreadyInitializedException.class,
            StockAlreadyExistsException.class,
            DuplicateProductInInitializationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidStockMovementException.class, InvalidStockAdjustmentException.class,
            InvalidStockThresholdException.class, InvalidStockInitializationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(StockLocationAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, message, req.getRequestURI()));
    }
}