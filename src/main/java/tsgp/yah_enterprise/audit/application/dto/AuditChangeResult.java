package tsgp.yah_enterprise.audit.application.dto;

public record AuditChangeResult(String fieldName, String oldValue, String newValue) { }
