package tsgp.yah_enterprise.payment.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.payment.domain.exception.*;

/** Maps the Payment exceptions to HTTP. Other modules' exceptions never reach this class. */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.payment.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaymentExceptionHandler {

    @ExceptionHandler({
            PaymentNotFoundException.class,
            ReceivableNotFoundException.class,
            CreditApprovalNotFoundException.class,
            PaymentCustomerNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({
            DuplicatePaymentReferenceException.class,
            PaymentAlreadyReversedException.class,
            PaymentOverAllocationException.class,
            ReceivableOverAllocationException.class,
            AllocationCustomerMismatchException.class,
            CreditApprovalAlreadyDecidedException.class,
            SelfCreditApprovalNotAllowedException.class,
            CreditNotAllowedForCustomerException.class,
            OpenCreditApprovalExistsException.class,
            CreditApprovalRequiredException.class,
            CreditApprovalMismatchException.class,
            PaymentCustomerNotUsableException.class,
            PaymentLocationNotUsableException.class,
            SaleAlreadySettledException.class,
            PaymentIdAlreadyUsedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidPaymentException.class,
            InvalidPaymentAmountException.class,
            InvalidCreditRequestException.class,
            SettlementOverpaymentException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalid(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(PaymentLocationAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(PaymentLocationAccessException ex,
                                                             HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(status).body(ApiResponse.error(status, message, req.getRequestURI()));
    }
}
