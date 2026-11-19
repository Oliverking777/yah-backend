package tsgp.yah_enterprise.purchasing.domain.repository;

import java.util.List;
import java.util.UUID;

public record PagedIds(List<UUID> items, long total) { }
