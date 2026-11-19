package tsgp.yah_enterprise.reports.application.dto;

public record ExportedReport(String filename, byte[] content, int rowCount) { }
