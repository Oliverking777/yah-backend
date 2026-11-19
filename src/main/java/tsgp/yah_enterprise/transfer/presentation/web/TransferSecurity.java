package tsgp.yah_enterprise.transfer.presentation.web;

/**
 * Expressions evaluated on the logged-in user (a record), so this module never imports IAM's classes.
 * ANY_LOCATION is true when the user's role works across every location (ADMIN, OWNER); everyone else
 * is limited to the location they are assigned to.
 */
final class TransferSecurity {

    static final String USER_ID = "userId()";
    static final String ANY_LOCATION = "!requiresLocation()";

    private TransferSecurity() { }
}
