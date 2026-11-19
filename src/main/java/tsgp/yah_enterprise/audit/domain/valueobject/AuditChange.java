package tsgp.yah_enterprise.audit.domain.valueobject;

import tsgp.yah_enterprise.audit.domain.exception.InvalidAuditEntryException;

import java.util.List;
import java.util.Locale;

/**
 * One field that changed: never a full snapshot. A value of a sensitive field (password, pin, token...) is masked as
 * "***" here, as a safety net: callers should not pass such values at all. Values are cut to 200 characters.
 */
public record AuditChange(String fieldName, String oldValue, String newValue) {

    public static final int MAX_FIELD_LENGTH = 60;
    public static final int MAX_VALUE_LENGTH = 200;
    public static final String MASK = "***";
    private static final List<String> SENSITIVE = List.of("password", "passcode", "pin", "token", "secret",
            "authorization", "credential");

    public AuditChange {
        String name = fieldName == null ? "" : fieldName.trim();
        if (name.isEmpty() || name.length() > MAX_FIELD_LENGTH) {
            throw new InvalidAuditEntryException("A changed field needs a name of 1 to " + MAX_FIELD_LENGTH
                    + " characters");
        }
        fieldName = name;
        boolean sensitive = isSensitive(name);
        oldValue = clean(oldValue, sensitive);
        newValue = clean(newValue, sensitive);
    }

    public static boolean isSensitive(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return SENSITIVE.stream().anyMatch(lower::contains);
    }

    private static String clean(String value, boolean sensitive) {
        if (value == null) {
            return null;
        }
        if (sensitive) {
            return MASK;
        }
        return value.length() > MAX_VALUE_LENGTH ? value.substring(0, MAX_VALUE_LENGTH) : value;
    }
}
