package tsgp.yah_enterprise.iam.domain.exception;

/**
 * Thrown when no free username could be generated (all the numbered variants are taken).
 */
public class UsernameGenerationException extends RuntimeException {

    public UsernameGenerationException(String base) {
        super("Could not generate a free username from: " + base);
    }
}