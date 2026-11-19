package tsgp.yah_enterprise.purchasing.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.purchasing.domain.exception.*;

/** Maps the Purchasing exceptions to HTTP. Other modules' exceptions never reach this class. */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.purchasing.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PurchasingExceptionHandler {

    @ExceptionHandler({
            PurchaseNotFoundException.class,
            PurchaseItemNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidPurchaseStateException.class,
            PurchaseSupplierNotUsableException.class,
            PurchaseLocationNotUsableException.class,
            PurchaseProductNotPurchasableException.class,
            DuplicatePurchaseItemException.class,
            OverReceiptException.class,
            PurchaseStockRejectedException.class,
            PurchaseIdAlreadyUsedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidPurchaseException.class,
            InvalidPurchaseQuantityException.class,
            InvalidPurchasePriceException.class,
            InvalidReceiptQuantityException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalid(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(PurchaseLocationAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(PurchaseLocationAccessException ex,
                                                             HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(status).body(ApiResponse.error(status, message, req.getRequestURI()));
    }
}
