package tsgp.yah_enterprise.purchasing.presentation.web;

/** Principal expressions shared by the Purchasing controllers. */
final class PurchasingSecurity {
    static final String USER_ID = "userId()";
    /** True for ADMIN, OWNER and MANAGER, who work across every location. */
    static final String ANY_LOCATION = "!requiresLocation()";

    private PurchasingSecurity() { }
}
