package tsgp.yah_enterprise.purchasing.domain.model;

import tsgp.yah_enterprise.purchasing.domain.exception.InvalidPurchaseException;

/** Small text rules shared by the aggregates. */
final class Text {

    private Text() { }

    static String required(String value, int min, int max, String what) {
        String v = value == null ? "" : value.trim();
        if (v.length() < min || v.length() > max) {
            throw new InvalidPurchaseException(what + " must be between " + min + " and " + max + " characters");
        }
        return v;
    }

    /** Null when blank, trimmed otherwise; fails when longer than max. */
    static String optional(String value, int max, String what) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim();
        if (v.length() > max) {
            throw new InvalidPurchaseException(what + " must be at most " + max + " characters");
        }
        return v;
    }
}
