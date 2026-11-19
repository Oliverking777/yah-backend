package tsgp.yah_enterprise.iam.domain.model;

/**
 * The kind of client a session was opened from.
 * WEB sessions carry the token in an HttpOnly cookie; MOBILE sessions carry it in a header.
 */
public enum ClientType {
    WEB,
    MOBILE
}