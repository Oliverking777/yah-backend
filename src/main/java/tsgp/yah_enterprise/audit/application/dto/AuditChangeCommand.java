package tsgp.yah_enterprise.audit.application.dto;

public record AuditChangeCommand(String fieldName, String oldValue, String newValue) { }
