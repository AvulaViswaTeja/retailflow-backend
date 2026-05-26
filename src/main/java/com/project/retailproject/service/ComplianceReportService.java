package com.project.retailproject.service;

import com.project.retailproject.db.ComplianceReportRepository;
import com.project.retailproject.dto.ComplianceReportRequestDTO;
import com.project.retailproject.dto.ComplianceReportResponseDTO;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.ComplianceReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplianceReportService {

    @Autowired
    private ComplianceReportRepository complianceReportRepository;

    @Autowired
    private AuditLogService auditLogService;

    // Thresholds
    private static final double SHRINKAGE_THRESHOLD = 5.0;
    private static final double MIN_STOCK_TURNOVER  = 2.0;
    private static final double MIN_SALES_GROWTH    = 0.0;


    // ── INSERT — now evaluates thresholds and gives verdict ───────────────────
    public ComplianceReportResponseDTO insertComplianceReport(
            ComplianceReportRequestDTO dto) {
        try {

            // Step 1 — Parse KPI values from metrics string
            // metrics = "Stock Turnover: 4.75 | Sales Growth: 12.0% | Shrinkage: 1.8%"
            double stockTurnover = 0.0;
            double salesGrowth   = 0.0;
            double shrinkageRate = 0.0;

            try {
                String metrics = dto.getMetrics();
                if (metrics != null && !metrics.isEmpty()) {
                    String[] parts = metrics.split("\\|");
                    for (String part : parts) {
                        part = part.trim();
                        if (part.startsWith("Stock Turnover:")) {
                            stockTurnover = Double.parseDouble(
                                    part.replace("Stock Turnover:", "").trim());
                        } else if (part.startsWith("Sales Growth:")) {
                            salesGrowth = Double.parseDouble(
                                    part.replace("Sales Growth:", "")
                                            .replace("%", "").trim());
                        } else if (part.startsWith("Shrinkage:")) {
                            shrinkageRate = Double.parseDouble(
                                    part.replace("Shrinkage:", "")
                                            .replace("%", "").trim());
                        }
                    }
                }
            } catch (Exception e) {
                // if parsing fails use 0 values
            }

            // Step 2 — Evaluate thresholds
            boolean shrinkageOk = shrinkageRate <= SHRINKAGE_THRESHOLD;
            boolean growthOk    = salesGrowth   >= MIN_SALES_GROWTH;
            boolean turnoverOk  = stockTurnover >= MIN_STOCK_TURNOVER;

            // Step 3 — Count failures
            int failures = 0;
            if (!shrinkageOk) failures++;
            if (!growthOk)    failures++;
            if (!turnoverOk)  failures++;

            // Step 4 — Determine verdict
            String complianceStatus = failures == 0 ? "PASS"
                    : failures >= 2 ? "FAIL"
                    : "WARNING";

            // Step 5 — Build remarks
            StringBuilder remarks = new StringBuilder();
            if (failures == 0) {
                remarks.append("All KPI thresholds met.");
            } else {
                if (!shrinkageOk) remarks.append(String.format(
                        "Shrinkage %.2f%% exceeds limit of %.1f%%. ",
                        shrinkageRate, SHRINKAGE_THRESHOLD));
                if (!growthOk) remarks.append(String.format(
                        "Sales growth is negative (%.2f%%). ", salesGrowth));
                if (!turnoverOk) remarks.append(String.format(
                        "Stock turnover %.2f is below minimum of %.1f.",
                        stockTurnover, MIN_STOCK_TURNOVER));
            }

            // Step 6 — Build and save entity
            ComplianceReport report = new ComplianceReport();
            report.setScope(dto.getScope());
            report.setMetrics(dto.getMetrics());
            report.setGeneratedDate(LocalDate.now());
            report.setStatus(complianceStatus);
            report.setRemarks(remarks.toString().trim());
            report.setStockTurnover(stockTurnover);
            report.setSalesGrowth(salesGrowth);
            report.setShrinkageRate(shrinkageRate);

            ComplianceReportResponseDTO result =
                    mapToDTO(complianceReportRepository.save(report));

            auditLogService.log("ComplianceReport.GENERATE_SUCCESS | ReportID: "
                    + result.getReportId()
                    + " | Scope: " + dto.getScope()
                    + " | Status: " + complianceStatus
                    + " | Remarks: " + remarks.toString().trim());

            return result;

        } catch (Exception ex) {
            auditLogService.logFailure("ComplianceReport.GENERATE", ex.getMessage());
            throw ex;
        }
    }


    // ── UPDATE — unchanged from original ─────────────────────────────────────
    public ComplianceReportResponseDTO updateComplianceReport(
            Long id, ComplianceReportRequestDTO dto) {
        ComplianceReport report = complianceReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compliance report not found with ID: " + id));

        String before = "Scope: " + report.getScope()
                + " | Metrics: " + report.getMetrics();

        try {
            report.setScope(dto.getScope());
            report.setMetrics(dto.getMetrics());

            ComplianceReportResponseDTO result =
                    mapToDTO(complianceReportRepository.save(report));

            auditLogService.log("ComplianceReport.UPDATE_SUCCESS | ReportID: " + id
                    + " | Before: " + before
                    + " | After: Scope: " + dto.getScope()
                    + " | Metrics: " + dto.getMetrics());

            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("ComplianceReport.UPDATE", ex.getMessage());
            throw ex;
        }
    }


    // ── DELETE — unchanged from original ─────────────────────────────────────
    public void deleteComplianceReport(Long id) {
        ComplianceReport report = complianceReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compliance report not found with ID: " + id));
        try {
            report.setStatus("ARCHIVED");
            complianceReportRepository.save(report);
            auditLogService.log("ComplianceReport.ARCHIVE_SUCCESS | ReportID: " + id
                    + " | Scope: " + report.getScope()
                    + " | Status: ARCHIVED");
        } catch (Exception ex) {
            auditLogService.logFailure("ComplianceReport.ARCHIVE", ex.getMessage());
            throw ex;
        }
    }


    // ── GET BY ID — unchanged ─────────────────────────────────────────────────
    public ComplianceReportResponseDTO getComplianceReport(Long id) {
        return mapToDTO(complianceReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compliance report not found with ID: " + id)));
    }


    // ── GET ALL — unchanged ───────────────────────────────────────────────────
    public List<ComplianceReportResponseDTO> getAllComplianceReports() {
        return complianceReportRepository.findAll()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    // ── GET BY SCOPE — unchanged ──────────────────────────────────────────────
    public List<ComplianceReportResponseDTO> getByScope(String scope) {
        return complianceReportRepository.findByScope(scope)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    // ── GET LATEST BY SCOPE — unchanged ──────────────────────────────────────
    public ComplianceReportResponseDTO getLatestByScope(String scope) {
        ComplianceReport report = complianceReportRepository
                .findFirstByScopeOrderByGeneratedDateDesc(scope);
        if (report == null) throw new ResourceNotFoundException(
                "No compliance report found for scope: " + scope);
        return mapToDTO(report);
    }


    // ── GET BY DATE RANGE — unchanged ─────────────────────────────────────────
    public List<ComplianceReportResponseDTO> getByDateRange(
            LocalDate start, LocalDate end) {
        return complianceReportRepository.findByGeneratedDateBetween(start, end)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    // ── PAGINATION — unchanged ────────────────────────────────────────────────
    public Page<ComplianceReportResponseDTO> getAllPagesWithPagination(
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return complianceReportRepository.findAll(pageable).map(this::mapToDTO);
    }


    // ── MAP ENTITY TO DTO — updated with new fields ───────────────────────────
    private ComplianceReportResponseDTO mapToDTO(ComplianceReport r) {
        ComplianceReportResponseDTO dto = new ComplianceReportResponseDTO();
        dto.setReportId(r.getReportId());
        dto.setScope(r.getScope());
        dto.setMetrics(r.getMetrics());
        dto.setGeneratedDate(r.getGeneratedDate());
        dto.setStatus(r.getStatus());
        dto.setRemarks(r.getRemarks());
        dto.setStockTurnover(r.getStockTurnover());
        dto.setSalesGrowth(r.getSalesGrowth());
        dto.setShrinkageRate(r.getShrinkageRate());
        return dto;
    }
}