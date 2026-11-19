package tsgp.yah_enterprise.category.application.dto;

/**
 * Input for listing categories page by page.
 * page is zero-based. size is limited to 1..100.
 */
public record CategoryPageRequest(
        int page,
        int size
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public CategoryPageRequest {
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

    public static CategoryPageRequest firstPage() {
        return new CategoryPageRequest(0, DEFAULT_SIZE);
    }
}