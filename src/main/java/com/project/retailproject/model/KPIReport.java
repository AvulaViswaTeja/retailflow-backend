package com.project.retailproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "kpi_report")
public class KPIReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @NotBlank(message = "Scope is required")
    private String scope;

    private String metrics;

    private LocalDate generatedDate;
    private String status;

    private Double stockTurnover;
    private Double salesGrowth;
    private Double shrinkageRate;

    private LocalDate currentStart;
    private LocalDate currentEnd;
    private LocalDate previousStart;
    private LocalDate previousEnd;

    public KPIReport() {}

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

    public Double getStockTurnover() { return stockTurnover; }
    public void setStockTurnover(Double stockTurnover) { this.stockTurnover = stockTurnover; }

    public Double getSalesGrowth() { return salesGrowth; }
    public void setSalesGrowth(Double salesGrowth) { this.salesGrowth = salesGrowth; }

    public Double getShrinkageRate() { return shrinkageRate; }
    public void setShrinkageRate(Double shrinkageRate) { this.shrinkageRate = shrinkageRate; }

    public LocalDate getCurrentStart() { return currentStart; }
    public void setCurrentStart(LocalDate currentStart) { this.currentStart = currentStart; }

    public LocalDate getCurrentEnd() { return currentEnd; }
    public void setCurrentEnd(LocalDate currentEnd) { this.currentEnd = currentEnd; }

    public LocalDate getPreviousStart() { return previousStart; }
    public void setPreviousStart(LocalDate previousStart) { this.previousStart = previousStart; }

    public LocalDate getPreviousEnd() { return previousEnd; }
    public void setPreviousEnd(LocalDate previousEnd) { this.previousEnd = previousEnd; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KPIReport that = (KPIReport) o;
        return Objects.equals(reportId, that.reportId);
    }

    @Override
    public int hashCode() { return Objects.hash(reportId); }
}