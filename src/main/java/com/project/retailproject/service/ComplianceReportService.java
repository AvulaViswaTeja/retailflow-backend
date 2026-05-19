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

    // Generate new report
    public ComplianceReportResponseDTO insertComplianceReport(ComplianceReportRequestDTO dto) {
        ComplianceReport report = new ComplianceReport();
        report.setScope(dto.getScope());
        report.setMetrics(dto.getMetrics());
        report.setGeneratedDate(LocalDate.now());
        report.setStatus("ACTIVE");
        return mapToDTO(complianceReportRepository.save(report));
    }

    // Update report
    public ComplianceReportResponseDTO updateComplianceReport(Long id, ComplianceReportRequestDTO dto) {
        ComplianceReport report = complianceReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compliance report not found with ID: " + id));
        report.setScope(dto.getScope());
        report.setMetrics(dto.getMetrics());
        return mapToDTO(complianceReportRepository.save(report));
    }

    // Delete (soft)
    public void deleteComplianceReport(Long id) {
        ComplianceReport report = complianceReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compliance report not found with ID: " + id));
        report.setStatus("ARCHIVED");
        complianceReportRepository.save(report);
    }

    // Get by ID
    public ComplianceReportResponseDTO getComplianceReport(Long id) {
        ComplianceReport report = complianceReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compliance report not found with ID: " + id));
        return mapToDTO(report);
    }

    // Get all
    public List<ComplianceReportResponseDTO> getAllComplianceReports() {
        return complianceReportRepository.findAll()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get by scope
    public List<ComplianceReportResponseDTO> getByScope(String scope) {
        return complianceReportRepository.findByScope(scope)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get latest by scope
    public ComplianceReportResponseDTO getLatestByScope(String scope) {
        ComplianceReport report = complianceReportRepository
                .findFirstByScopeOrderByGeneratedDateDesc(scope);
        if (report == null) throw new ResourceNotFoundException(
                "No compliance report found for scope: " + scope);
        return mapToDTO(report);
    }

    // Get by date range
    public List<ComplianceReportResponseDTO> getByDateRange(LocalDate start, LocalDate end) {
        return complianceReportRepository.findByGeneratedDateBetween(start, end)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Pagination
    public Page<ComplianceReportResponseDTO> getAllPagesWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return complianceReportRepository.findAll(pageable).map(this::mapToDTO);
    }

    // --- Mapper ---
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