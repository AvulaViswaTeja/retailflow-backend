package com.project.retailproject.dto;

import jakarta.validation.constraints.NotBlank;

public class KPIReportRequestDTO {

    @NotBlank(message = "Scope is required")
    private String scope;



    @NotBlank(message = "Metrics is required")
    private String metrics;

    // Getters & Setters
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }
}