package tsgp.yah_enterprise.audit.domain.repository;

import tsgp.yah_enterprise.audit.domain.model.AuditEntry;

import java.util.List;

public record PagedAuditEntries(List<AuditEntry> items, long total) { }
