package tsgp.yah_enterprise.iam.domain.exception;

import java.util.UUID;

public class SessionExpiredException extends RuntimeException {

    public SessionExpiredException(UUID sessionId) {
        super("The session with id: " + sessionId + " has expired");
    }
}