package tsgp.yah_enterprise.iam.application.port.in;

public interface LogoutUseCase {

    /**
     * Ends the session that belongs to the given token.
     * Does nothing if the token is malformed or matches no session, so logging out twice is harmless.
     */
    void logout(String sessionToken);
}