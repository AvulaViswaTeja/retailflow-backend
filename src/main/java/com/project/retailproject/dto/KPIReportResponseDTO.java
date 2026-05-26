package com.project.retailproject.dto;

import java.time.LocalDate;

public class KPIReportResponseDTO {

    private Long reportId;
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
}