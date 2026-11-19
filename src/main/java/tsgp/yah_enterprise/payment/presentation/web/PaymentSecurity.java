package tsgp.yah_enterprise.payment.presentation.web;

/** Expressions evaluated on the logged-in user (a record), so this module never imports IAM's classes. */
final class PaymentSecurity {

    static final String USER_ID = "userId()";
    static final String ANY_LOCATION = "!requiresLocation()";

    private PaymentSecurity() { }
}
