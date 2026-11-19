package tsgp.yah_enterprise.sales.application.dto;

/** Paging rules: page is at least 0, size is 1 to 100 (default 20). */
final class PageLimits {
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 100;

    private PageLimits() { }

    static int page(int page) {
        return Math.max(page, 0);
    }

    static int size(int size) {
        if (size < 1) return DEFAULT_SIZE;
        return Math.min(size, MAX_SIZE);
    }
}
