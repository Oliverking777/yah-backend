package tsgp.yah_enterprise.location.domain.model;

import tsgp.yah_enterprise.location.domain.exception.InvalidLocationException;

import java.util.regex.Pattern;

/** Human-readable, unique and immutable business identifier, e.g. WH-MAIN, STORE-01. */
public record LocationCode(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[A-Z0-9][A-Z0-9_-]{1,29}$");

    public LocationCode {
        if (value == null || value.isBlank()) {
            throw new InvalidLocationException("Location code is required");
        }
        value = value.trim().toUpperCase();
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidLocationException(
                "Location code must be 2-30 characters: letters, digits, '-' or '_'");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}