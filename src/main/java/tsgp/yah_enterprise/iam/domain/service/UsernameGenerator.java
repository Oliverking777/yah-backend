package tsgp.yah_enterprise.iam.domain.service;

import java.text.Normalizer;
import java.util.function.Predicate;

import tsgp.yah_enterprise.iam.domain.exception.UsernameGenerationException;
import tsgp.yah_enterprise.iam.domain.valueobject.Username;

/**
 * Generates a username from a person's first and last name.
 * <p>
 * Format: firstname.lastname, lower case, accents removed, anything except letters and digits dropped.
 * If that name is taken, a number is added: john.doe, john.doe2, john.doe3 ...
 * <p>
 * Examples: "John" "Doe" -> john.doe;  "José" "García-López" -> jose.garcialopez
 * <p>
 * This is pure domain logic: whether a name is already taken is decided by the caller through
 * the alreadyTaken check (normally UserRepository::existsByUserName).
 */
public final class UsernameGenerator {

    private static final int MAX_BASE_LENGTH = 40; // leaves room for the number suffix within the 50-character limit
    private static final int MIN_BASE_LENGTH = 3;
    private static final int MAX_ATTEMPTS = 1000;
    private static final String FALLBACK = "user";

    private UsernameGenerator() {
    }

    public static Username generate(String firstName, String lastName, Predicate<Username> alreadyTaken) {
        String base = buildBase(firstName, lastName);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String candidate = attempt == 1 ? base : base + attempt;
            Username username = Username.of(candidate);
            if (!alreadyTaken.test(username)) {
                return username;
            }
        }
        throw new UsernameGenerationException(base);
    }

    private static String buildBase(String firstName, String lastName) {
        String first = clean(firstName);
        String last = clean(lastName);

        String base;
        if (!first.isEmpty() && !last.isEmpty()) {
            base = first + "." + last;
        } else if (!first.isEmpty()) {
            base = first;
        } else if (!last.isEmpty()) {
            base = last;
        } else {
            base = FALLBACK;
        }

        if (base.length() > MAX_BASE_LENGTH) {
            base = base.substring(0, MAX_BASE_LENGTH).replaceAll("\\.+$", ""); // never end with a dot
        }
        if (base.length() < MIN_BASE_LENGTH) {
            base = base + FALLBACK;
        }
        return base;
    }

    /** Lower-cases, strips accents, and keeps only a-z and 0-9. */
    private static String clean(String name) {
        if (name == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return withoutAccents.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}