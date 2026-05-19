package com.project.retailproject.dto;

import java.time.LocalDate;

public class ComplianceReportResponseDTO {

    private Long reportId;
    private String scope;
    private String metrics;
    private LocalDate generatedDate;
    private String status;

    // Getters & Setters
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }
    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}