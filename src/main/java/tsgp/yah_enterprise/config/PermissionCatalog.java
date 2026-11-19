package tsgp.yah_enterprise.config;

import java.util.List;

/**
 * The single list of every permission code the system uses, as Java constants.
 * <p>
 * - The codes are created in the database by the Flyway migrations (V10 and later), NOT at startup.
 * - Controllers use these constants, so a typo is a compile error instead of a silent 403:
 *   {@code @PreAuthorize("hasAuthority('" + PermissionCatalog.ROLE_CREATE + "')")}
 * <p>
 * To add a permission: add a constant here AND to {@link #ALL}, AND add it in a new Flyway migration
 * (insert the code, then grant it to ADMIN and OWNER). Every code must follow the PermissionCode rules
 * (upper case, single underscores). Never rename a code that is already in use.
 */
public final class PermissionCatalog {

    // ---- 01 IAM ----
    public static final String PERMISSION_CREATE = "PERMISSION_CREATE";
    public static final String PERMISSION_READ = "PERMISSION_READ";
    public static final String PERMISSION_UPDATE = "PERMISSION_UPDATE";
    public static final String PERMISSION_DELETE = "PERMISSION_DELETE";
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_DELETE = "ROLE_DELETE";
    public static final String ROLE_PERMISSION_READ = "ROLE_PERMISSION_READ";
    public static final String ROLE_PERMISSION_UPDATE = "ROLE_PERMISSION_UPDATE";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_READ = "USER_READ";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String USER_ACTIVATE = "USER_ACTIVATE";
    public static final String USER_DEACTIVATE = "USER_DEACTIVATE";
    public static final String USER_ASSIGN_ROLE = "USER_ASSIGN_ROLE";
    public static final String USER_PASSWORD_RESET = "USER_PASSWORD_RESET";

    // ---- 02 Location ----
    public static final String LOCATION_CREATE = "LOCATION_CREATE";
    public static final String LOCATION_READ = "LOCATION_READ";
    public static final String LOCATION_UPDATE = "LOCATION_UPDATE";
    public static final String LOCATION_ACTIVATE = "LOCATION_ACTIVATE";
    public static final String LOCATION_DEACTIVATE = "LOCATION_DEACTIVATE";
    public static final String LOCATION_USER_READ = "LOCATION_USER_READ";
    public static final String LOCATION_USER_ASSIGN = "LOCATION_USER_ASSIGN";
    public static final String LOCATION_USER_REMOVE = "LOCATION_USER_REMOVE";

    // ---- 03 Category ----
    public static final String CATEGORY_CREATE = "CATEGORY_CREATE";
    public static final String CATEGORY_READ = "CATEGORY_READ";
    public static final String CATEGORY_UPDATE = "CATEGORY_UPDATE";
    public static final String CATEGORY_ACTIVATE = "CATEGORY_ACTIVATE";
    public static final String CATEGORY_DEACTIVATE = "CATEGORY_DEACTIVATE";

    // ---- 04 Product ----
    public static final String PRODUCT_CREATE = "PRODUCT_CREATE";
    public static final String PRODUCT_READ = "PRODUCT_READ";
    public static final String PRODUCT_UPDATE = "PRODUCT_UPDATE";
    public static final String PRODUCT_ACTIVATE = "PRODUCT_ACTIVATE";
    public static final String PRODUCT_DEACTIVATE = "PRODUCT_DEACTIVATE";

    // ---- 05 Unit & Conversion ----
    public static final String UNIT_CREATE = "UNIT_CREATE";
    public static final String UNIT_READ = "UNIT_READ";
    public static final String UNIT_UPDATE = "UNIT_UPDATE";
    public static final String UNIT_DEACTIVATE = "UNIT_DEACTIVATE";

    // ---- 06 Pricing ----
    public static final String PRICE_RULE_CREATE = "PRICE_RULE_CREATE";
    public static final String PRICE_RULE_READ = "PRICE_RULE_READ";
    public static final String PRICE_RULE_UPDATE = "PRICE_RULE_UPDATE";
    public static final String PRICE_RULE_DEACTIVATE = "PRICE_RULE_DEACTIVATE";

    // ---- 07 Customer ----
    public static final String CUSTOMER_CREATE = "CUSTOMER_CREATE";
    public static final String CUSTOMER_READ = "CUSTOMER_READ";
    public static final String CUSTOMER_UPDATE = "CUSTOMER_UPDATE";
    public static final String CUSTOMER_DEACTIVATE = "CUSTOMER_DEACTIVATE";
    public static final String CUSTOMER_HISTORY_READ = "CUSTOMER_HISTORY_READ";

    // ---- 08 Supplier / Purchasing ----
    public static final String SUPPLIER_CREATE = "SUPPLIER_CREATE";
    public static final String SUPPLIER_READ = "SUPPLIER_READ";
    public static final String SUPPLIER_UPDATE = "SUPPLIER_UPDATE";
    public static final String SUPPLIER_DEACTIVATE = "SUPPLIER_DEACTIVATE";
    public static final String PURCHASE_CREATE = "PURCHASE_CREATE";
    public static final String PURCHASE_READ = "PURCHASE_READ";
    public static final String PURCHASE_BACKDATE = "PURCHASE_BACKDATE";
    public static final String PURCHASE_CANCEL = "PURCHASE_CANCEL";
    /** Confirms the delivery of a purchase: the accepted goods go into stock. */
    public static final String RECEIPT_CONFIRM = "RECEIPT_CONFIRM";

    // ---- 09 Inventory ----
    public static final String STOCK_READ = "STOCK_READ";
    public static final String STOCK_READ_ALL = "STOCK_READ_ALL";
    public static final String STOCK_ADJUST_CREATE = "STOCK_ADJUST_CREATE";
    public static final String STOCK_ADJUST_READ = "STOCK_ADJUST_READ";
    public static final String STOCK_ADJUST_APPROVE = "STOCK_ADJUST_APPROVE";
    public static final String STOCK_OVERSELL_OVERRIDE = "STOCK_OVERSELL_OVERRIDE";
    public static final String STOCK_THRESHOLD_READ = "STOCK_THRESHOLD_READ";
    public static final String STOCK_THRESHOLD_UPDATE = "STOCK_THRESHOLD_UPDATE";
    public static final String STOCK_INIT_CREATE = "STOCK_INIT_CREATE";
    public static final String STOCK_INIT_READ = "STOCK_INIT_READ";

    // ---- 10 Transfer ----
    public static final String TRANSFER_CREATE = "TRANSFER_CREATE";
    public static final String TRANSFER_READ = "TRANSFER_READ";
    public static final String TRANSFER_READ_ALL = "TRANSFER_READ_ALL";
    public static final String TRANSFER_CONFIRM = "TRANSFER_CONFIRM";

    // ---- 11 Sales / Orders ----
    public static final String SALE_CREATE = "SALE_CREATE";
    public static final String SALE_READ = "SALE_READ";
    public static final String SALE_READ_ALL = "SALE_READ_ALL";
    public static final String SALE_COMPLETE = "SALE_COMPLETE";
    public static final String SALE_VALIDATE = "SALE_VALIDATE";
    public static final String SALE_REVERSE = "SALE_REVERSE";

    // ---- 12 Payment / Credit ----
    public static final String PAYMENT_CREATE = "PAYMENT_CREATE";
    public static final String PAYMENT_READ = "PAYMENT_READ";
    public static final String BALANCE_READ = "BALANCE_READ";
    public static final String CREDIT_APPROVE = "CREDIT_APPROVE";

    // ---- 13 Offline Sync ----
    public static final String SYNC_PUSH = "SYNC_PUSH";
    public static final String SYNC_PULL = "SYNC_PULL";
    public static final String SYNC_STATUS_READ = "SYNC_STATUS_READ";
    public static final String SYNC_CONFLICT_READ = "SYNC_CONFLICT_READ";
    public static final String SYNC_CONFLICT_RESOLVE = "SYNC_CONFLICT_RESOLVE";

    // ---- 14 Audit ----
    public static final String AUDIT_READ = "AUDIT_READ";
    public static final String AUDIT_EXPORT = "AUDIT_EXPORT";

    // ---- 15 Dashboard / Reports ----
    public static final String DASHBOARD_READ = "DASHBOARD_READ";
    public static final String DASHBOARD_SUMMARY_READ = "DASHBOARD_SUMMARY_READ";
    public static final String REPORT_SALES_READ = "REPORT_SALES_READ";
    public static final String REPORT_STOCK_READ = "REPORT_STOCK_READ";
    public static final String REPORT_DEBT_READ = "REPORT_DEBT_READ";
    public static final String ALERT_READ = "ALERT_READ";
    public static final String REPORT_VIEW = "REPORT_VIEW";
    public static final String REPORT_VIEW_ALL = "REPORT_VIEW_ALL";
    public static final String REPORT_EXPORT = "REPORT_EXPORT";
    public static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";
    public static final String DASHBOARD_VIEW_ALL = "DASHBOARD_VIEW_ALL";

    /** Every permission code of the system. Keep it in sync with the constants above and the migrations. */
    public static final List<String> ALL = List.of(
            // 01 IAM
            PERMISSION_CREATE, PERMISSION_READ, PERMISSION_UPDATE, PERMISSION_DELETE, ROLE_CREATE, ROLE_READ, ROLE_UPDATE, ROLE_DELETE, ROLE_PERMISSION_READ, ROLE_PERMISSION_UPDATE, USER_CREATE, USER_READ, USER_UPDATE, USER_DELETE, USER_ACTIVATE, USER_DEACTIVATE, USER_ASSIGN_ROLE, USER_PASSWORD_RESET,
            // 02 Location
            LOCATION_CREATE, LOCATION_READ, LOCATION_UPDATE, LOCATION_ACTIVATE, LOCATION_DEACTIVATE, LOCATION_USER_READ, LOCATION_USER_ASSIGN, LOCATION_USER_REMOVE,
            // 03 Category
            CATEGORY_CREATE, CATEGORY_READ, CATEGORY_UPDATE, CATEGORY_ACTIVATE, CATEGORY_DEACTIVATE,
            // 04 Product
            PRODUCT_CREATE, PRODUCT_READ, PRODUCT_UPDATE, PRODUCT_ACTIVATE, PRODUCT_DEACTIVATE,
            // 05 Unit & Conversion
            UNIT_CREATE, UNIT_READ, UNIT_UPDATE, UNIT_DEACTIVATE,
            // 06 Pricing
            PRICE_RULE_CREATE, PRICE_RULE_READ, PRICE_RULE_UPDATE, PRICE_RULE_DEACTIVATE,
            // 07 Customer
            CUSTOMER_CREATE, CUSTOMER_READ, CUSTOMER_UPDATE, CUSTOMER_DEACTIVATE, CUSTOMER_HISTORY_READ,
            // 08 Supplier / Purchasing
            SUPPLIER_CREATE, SUPPLIER_READ, SUPPLIER_UPDATE, SUPPLIER_DEACTIVATE, PURCHASE_CREATE, PURCHASE_READ, PURCHASE_BACKDATE,
            PURCHASE_CANCEL, RECEIPT_CONFIRM,
            // 09 Inventory
            STOCK_READ, STOCK_ADJUST_CREATE, STOCK_ADJUST_READ, STOCK_ADJUST_APPROVE, STOCK_OVERSELL_OVERRIDE, STOCK_THRESHOLD_READ, STOCK_THRESHOLD_UPDATE, STOCK_INIT_CREATE, STOCK_INIT_READ,
            // 10 Transfer
            TRANSFER_CREATE, TRANSFER_READ,  TRANSFER_CONFIRM,
            // 11 Sales / Orders
            SALE_CREATE, SALE_READ, SALE_COMPLETE, SALE_VALIDATE, SALE_REVERSE,
            // 12 Payment / Credit
            PAYMENT_CREATE, PAYMENT_READ, BALANCE_READ, CREDIT_APPROVE,
            // 13 Offline Sync
            SYNC_PUSH, SYNC_PULL, SYNC_STATUS_READ, SYNC_CONFLICT_READ, SYNC_CONFLICT_RESOLVE,
            // 14 Audit
            AUDIT_READ, AUDIT_EXPORT,
            // 15 Dashboard / Reports
            DASHBOARD_READ, DASHBOARD_SUMMARY_READ, REPORT_SALES_READ, REPORT_STOCK_READ, REPORT_DEBT_READ, ALERT_READ,
            REPORT_VIEW, REPORT_VIEW_ALL, REPORT_EXPORT, DASHBOARD_VIEW, DASHBOARD_VIEW_ALL
    );

    private PermissionCatalog() {
    }
}