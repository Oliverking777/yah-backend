package tsgp.yah_enterprise.sales.presentation.web;

/** Expressions evaluated on the logged-in user (a record), so this module never imports IAM's classes. */
final class SalesSecurity {

    static final String USER_ID = "userId()";
    static final String ANY_LOCATION = "!requiresLocation()";

    private SalesSecurity() { }
}
