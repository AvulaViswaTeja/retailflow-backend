package com.project.retailproject.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class KPIReportRequestDTO {

    @NotBlank(message = "Scope is required")
    private String scope;

    private String metrics;

    private LocalDate currentStart;
    private LocalDate currentEnd;
    private LocalDate previousStart;
    private LocalDate previousEnd;

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }

    public LocalDate getCurrentStart() { return currentStart; }
    public void setCurrentStart(LocalDate currentStart) { this.currentStart = currentStart; }

    public LocalDate getCurrentEnd() { return currentEnd; }
    public void setCurrentEnd(LocalDate currentEnd) { this.currentEnd = currentEnd; }

    public LocalDate getPreviousStart() { return previousStart; }
    public void setPreviousStart(LocalDate previousStart) { this.previousStart = previousStart; }

    public LocalDate getPreviousEnd() { return previousEnd; }
    public void setPreviousEnd(LocalDate previousEnd) { this.previousEnd = previousEnd; }
}