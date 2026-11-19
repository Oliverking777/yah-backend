package tsgp.yah_enterprise.transfer.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.transfer.domain.exception.*;

/** Maps the Transfer exceptions to HTTP. Other modules' exceptions never reach this class. */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.transfer.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransferExceptionHandler {

    @ExceptionHandler(TransferNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(TransferNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidTransferStateException.class,
            TransferLocationNotUsableException.class,
            TransferProductNotTransferableException.class,
            DuplicateTransferItemException.class,
            TransferStockUnavailableException.class,
            TransferIdAlreadyUsedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidTransferException.class,
            InvalidTransferQuantityException.class,
            SameLocationTransferException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalid(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(TransferLocationAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(TransferLocationAccessException ex,
                                                             HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(status).body(ApiResponse.error(status, message, req.getRequestURI()));
    }
}
