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

    public ComplianceReportResponseDTO insertComplianceReport(ComplianceReportRequestDTO dto) {
        try {
            ComplianceReport report = new ComplianceReport();
            report.setScope(dto.getScope());
            report.setMetrics(dto.getMetrics());
            report.setGeneratedDate(LocalDate.now());
            report.setStatus("ACTIVE");

            ComplianceReportResponseDTO result = mapToDTO(complianceReportRepository.save(report));
            auditLogService.log("ComplianceReport.GENERATE_SUCCESS | ReportID: " + result.getReportId()
                    + " | Scope: " + dto.getScope()
                    + " | GeneratedDate: " + result.getGeneratedDate()
                    + " | Status: ACTIVE");
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("ComplianceReport.GENERATE", ex.getMessage());
            throw ex;
        }
    }

    public ComplianceReportResponseDTO updateComplianceReport(Long id, ComplianceReportRequestDTO dto) {
        ComplianceReport report = complianceReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compliance report not found with ID: " + id));

        String before = "Scope: " + report.getScope()
                + " | Metrics: " + report.getMetrics();

        try {
            report.setScope(dto.getScope());
            report.setMetrics(dto.getMetrics());

            ComplianceReportResponseDTO result = mapToDTO(complianceReportRepository.save(report));
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

    public ComplianceReportResponseDTO getComplianceReport(Long id) {
        return mapToDTO(complianceReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compliance report not found with ID: " + id)));
    }

    public List<ComplianceReportResponseDTO> getAllComplianceReports() {
        return complianceReportRepository.findAll()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ComplianceReportResponseDTO> getByScope(String scope) {
        return complianceReportRepository.findByScope(scope)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ComplianceReportResponseDTO getLatestByScope(String scope) {
        ComplianceReport report = complianceReportRepository
                .findFirstByScopeOrderByGeneratedDateDesc(scope);
        if (report == null) throw new ResourceNotFoundException(
                "No compliance report found for scope: " + scope);
        return mapToDTO(report);
    }

    public List<ComplianceReportResponseDTO> getByDateRange(LocalDate start, LocalDate end) {
        return complianceReportRepository.findByGeneratedDateBetween(start, end)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Page<ComplianceReportResponseDTO> getAllPagesWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return complianceReportRepository.findAll(pageable).map(this::mapToDTO);
    }


    private ComplianceReportResponseDTO mapToDTO(ComplianceReport r) {
        ComplianceReportResponseDTO dto = new ComplianceReportResponseDTO();
        dto.setReportId(r.getReportId());
        dto.setScope(r.getScope());
        dto.setMetrics(r.getMetrics());
        dto.setGeneratedDate(r.getGeneratedDate());
        dto.setStatus(r.getStatus());
        return dto;
    }
}