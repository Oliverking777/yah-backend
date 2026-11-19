package tsgp.yah_enterprise.customer.application.dto;

import tsgp.yah_enterprise.customer.domain.model.CustomerStatus;

public record CustomerPageRequest(
        int page,
        int size,
        String search,
        CustomerStatus status
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public CustomerPageRequest {
        if (page < 0) page = 0;
        if (size < 1) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }
}
