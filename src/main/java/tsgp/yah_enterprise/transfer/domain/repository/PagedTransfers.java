package tsgp.yah_enterprise.transfer.domain.repository;

import tsgp.yah_enterprise.transfer.domain.model.Transfer;

import java.util.List;

/** One page of transfers plus the total number of matching transfers. */
public record PagedTransfers(List<Transfer> items, long total) {
    public PagedTransfers {
        items = List.copyOf(items);
    }
}
