package com.project.retailproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "compliance_report")
public class ComplianceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @NotBlank(message = "Scope is required")
    private String scope;

    @NotBlank(message = "Metrics is required")
    private String metrics;

    private LocalDate generatedDate;

    private String status;

    public ComplianceReport() {}

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ComplianceReport that = (ComplianceReport) o;
        return Objects.equals(reportId, that.reportId);
    }

    @Override
    public int hashCode() { return Objects.hash(reportId); }
}