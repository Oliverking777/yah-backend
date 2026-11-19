package tsgp.yah_enterprise.audit.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.audit.domain.exception.*;

/** Maps the Audit exceptions to HTTP. Other modules' exceptions never reach this class. */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.audit.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuditExceptionHandler {

    @ExceptionHandler(AuditEntryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidAuditEntryException.class,
            InvalidAuditQueryException.class,
            AuditExportTooLargeException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalid(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(status).body(ApiResponse.error(status, message, req.getRequestURI()));
    }
}
