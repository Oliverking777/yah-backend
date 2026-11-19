package tsgp.yah_enterprise.unit.application.dto;

/**
 * Input for listing units page by page. page is zero-based, size is limited to 1..100.
 * status is optional: ACTIVE or INACTIVE (any case), or null/blank for all units.
 */
public record UnitPageRequest(
        int page,
        int size,
        String status
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public UnitPageRequest {
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
    }

    public UnitPageRequest(int page, int size) {
        this(page, size, null);
    }

    public static UnitPageRequest firstPage() {
        return new UnitPageRequest(0, DEFAULT_SIZE, null);
    }
}
