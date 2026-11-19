package tsgp.yah_enterprise.payment.domain.repository;

import java.util.List;

/** One page of results plus the total count. */
public record PageSlice<T>(List<T> items, long total) {
}
