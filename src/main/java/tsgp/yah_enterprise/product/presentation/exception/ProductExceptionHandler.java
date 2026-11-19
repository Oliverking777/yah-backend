package tsgp.yah_enterprise.product.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.product.domain.exception.*;

@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.product.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProductExceptionHandler {

    @ExceptionHandler({
            ProductNotFoundException.class, ProductUnitNotFoundException.class,
            VariantNotFoundException.class, VariantCodeNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({
            ProductCodeAlreadyExistsException.class, ProductAlreadyActiveException.class,
            ProductAlreadyInactiveException.class, ProductInactiveException.class,
            ProductUnitAlreadyAssignedException.class, BaseUnitProtectedException.class,
            CategoryNotAssignableException.class, UnitNotAssignableException.class,
            DuplicateVariantNameException.class, VariantNameRequiredException.class,
            VariantSkuAlreadyExistsException.class,
            VariantAlreadyActiveException.class, VariantAlreadyInactiveException.class,
            VariantInactiveException.class, LastActiveVariantException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({InvalidProductException.class, InvalidConversionException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, message, req.getRequestURI()));
    }
}