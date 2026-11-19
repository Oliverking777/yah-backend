package tsgp.yah_enterprise.iam.application.dto;

import java.time.Instant;

import tsgp.yah_enterprise.iam.domain.model.ClientType;

/**
 * Output of a successful login.
 * sessionToken is the only time the raw token is ever available: the server keeps just its hash.
 * The presentation layer decides how to hand it over (cookie for WEB, response body for MOBILE).
 */
public record LoginResult(
        String sessionToken,
        Instant expiresAt,
        ClientType clientType,
        UserResult user
) {
    @Override
    public String toString() {
        return "LoginResult[sessionToken=protected, expiresAt=" + expiresAt + ", clientType=" + clientType
                + ", user=" + user + "]";
    }
}