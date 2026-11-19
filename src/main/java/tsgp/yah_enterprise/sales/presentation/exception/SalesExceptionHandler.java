package tsgp.yah_enterprise.sales.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.sales.domain.exception.*;

/** Maps the Sales exceptions to HTTP. Other modules' exceptions never reach this class. */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.sales.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SalesExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(OrderNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidOrderStateException.class,
            SelfValidationNotAllowedException.class,
            OrderItemNotPriceableException.class,
            OrderCustomerNotUsableException.class,
            OrderLocationNotUsableException.class,
            OrderProductNotSellableException.class,
            DuplicateOrderItemException.class,
            OrderStockUnavailableException.class,
            SaleSettlementRejectedException.class,
            ReceiptNotAvailableException.class,
            OrderIdAlreadyUsedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidOrderException.class,
            InvalidOrderQuantityException.class,
            InvalidDiscountException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalid(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(OrderLocationAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(OrderLocationAccessException ex,
                                                             HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(status).body(ApiResponse.error(status, message, req.getRequestURI()));
    }
}
