package tsgp.yah_enterprise.category.presentation.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import tsgp.yah_enterprise.category.domain.exception.CategoryAlreadyActiveException;
import tsgp.yah_enterprise.category.domain.exception.CategoryAlreadyInactiveException;
import tsgp.yah_enterprise.category.domain.exception.CategoryNameAlreadyExistsException;
import tsgp.yah_enterprise.category.domain.exception.CategoryNotFoundException;
import tsgp.yah_enterprise.category.domain.exception.InvalidCategoryException;
import tsgp.yah_enterprise.config.ApiResponse;

/**
 * Turns Category domain exceptions into ApiResponse bodies.
 * <p>
 * It only handles Category exceptions and only for Category controllers, and it runs before the
 * IAM handler. Everything else (validation errors, bad JSON, access denied, unexpected errors) is
 * left to the shared GlobalExceptionHandler, so the response format stays the same.
 */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.category.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CategoryExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(CategoryNotFoundException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({CategoryNameAlreadyExistsException.class, CategoryAlreadyActiveException.class,
            CategoryAlreadyInactiveException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidInput(InvalidCategoryException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, message, request.getRequestURI()));
    }
}