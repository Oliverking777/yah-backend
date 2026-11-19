package tsgp.yah_enterprise.iam.domain.exception;

import java.util.UUID;

public class CredentialNotFoundException extends RuntimeException {

    public CredentialNotFoundException(UUID userId) {
        super("No credential was found for user with id: " + userId);
    }
}