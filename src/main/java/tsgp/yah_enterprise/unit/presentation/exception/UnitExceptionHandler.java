package tsgp.yah_enterprise.unit.presentation.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.unit.domain.exception.InvalidUnitException;
import tsgp.yah_enterprise.unit.domain.exception.UnitAlreadyActiveException;
import tsgp.yah_enterprise.unit.domain.exception.UnitAlreadyInactiveException;
import tsgp.yah_enterprise.unit.domain.exception.UnitCodeAlreadyExistsException;
import tsgp.yah_enterprise.unit.domain.exception.UnitInactiveException;
import tsgp.yah_enterprise.unit.domain.exception.UnitNameAlreadyExistsException;
import tsgp.yah_enterprise.unit.domain.exception.UnitNotFoundException;

/**
 * Turns Unit domain exceptions into ApiResponse bodies.
 * It only handles Unit exceptions and only for Unit controllers. Everything else (validation errors,
 * bad JSON, access denied, unexpected errors) is left to the shared GlobalExceptionHandler.
 */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.unit.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UnitExceptionHandler {

    @ExceptionHandler(UnitNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(UnitNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({UnitNameAlreadyExistsException.class, UnitCodeAlreadyExistsException.class,
            UnitAlreadyActiveException.class, UnitAlreadyInactiveException.class, UnitInactiveException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidUnitException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidInput(InvalidUnitException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, message, request.getRequestURI()));
    }
}
