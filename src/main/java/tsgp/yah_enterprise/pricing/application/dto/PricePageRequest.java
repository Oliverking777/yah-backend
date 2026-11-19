package tsgp.yah_enterprise.pricing.application.dto;

/**
 * Input for listing prices page by page.
 * page is zero-based. size is limited to 1..100.
 */
public record PricePageRequest(
        int page,
        int size
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PricePageRequest {
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

    public static PricePageRequest firstPage() {
        return new PricePageRequest(0, DEFAULT_SIZE);
    }
}
