package tsgp.yah_enterprise.audit.domain.valueobject;

import tsgp.yah_enterprise.audit.domain.exception.InvalidAuditEntryException;

import java.util.regex.Pattern;

/** MODULE.ACTION in upper case, for example PURCHASE.ORDERED. At most 80 characters. */
public record AuditAction(String value) {

    public static final int MAX_LENGTH = 80;
    private static final Pattern FORMAT = Pattern.compile("^[A-Z][A-Z0-9_]*(\\.[A-Z][A-Z0-9_]*)+$");

    public AuditAction {
        if (value == null || value.length() > MAX_LENGTH || !FORMAT.matcher(value).matches()) {
            throw new InvalidAuditEntryException(
                    "Action must look like MODULE.ACTION in upper case (at most " + MAX_LENGTH + " characters)");
        }
    }

    public static AuditAction of(String value) {
        return new AuditAction(value == null ? null : value.trim());
    }

    @Override
    public String toString() {
        return value;
    }
}
