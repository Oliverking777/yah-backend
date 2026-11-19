package tsgp.yah_enterprise.config;

/**
 * The built-in roles that code may refer to, and the authority a logged-in user gets for their role.
 * <p>
 * Besides one authority per permission code (e.g. USER_CREATE), every logged-in user gets ONE authority
 * for their role: "ROLE:" + role name (e.g. "ROLE:OWNER"). Use it for the few actions that only a specific
 * role may do, on top of the permission:
 * {@code @PreAuthorize("hasAuthority('" + PermissionCatalog.USER_DELETE + "') and hasAuthority('"
 * + RoleCatalog.OWNER_AUTHORITY + "')")}
 * <p>
 * Always use hasAuthority, not hasRole (hasRole would add a second "ROLE_" prefix).
 * The ':' can never appear in a permission code, so a role authority can never clash with a permission.
 * Only refer to SYSTEM roles here: they cannot be renamed or deleted, so the name stays valid.
 */
public final class RoleCatalog {

    public static final String OWNER = "OWNER";
    public static final String ADMIN = "ADMIN";

    public static final String AUTHORITY_PREFIX = "ROLE:";
    public static final String OWNER_AUTHORITY = AUTHORITY_PREFIX + OWNER;
    public static final String ADMIN_AUTHORITY = AUTHORITY_PREFIX + ADMIN;

    /** The authority granted to a user whose role is {@code roleName}. */
    public static String authorityOf(String roleName) {
        return AUTHORITY_PREFIX + roleName;
    }

    private RoleCatalog() {
    }
}