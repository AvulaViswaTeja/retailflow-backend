package com.project.retailproject.service;

import com.project.retailproject.db.KPIReportRepository;
import com.project.retailproject.dto.KPIReportRequestDTO;
import com.project.retailproject.dto.KPIReportResponseDTO;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.KPIReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KPIReportService {

    @Autowired
    private KPIReportRepository kpiReportRepository;

    // Save KPI report
    public KPIReportResponseDTO saveReport(KPIReportRequestDTO dto) {
        KPIReport report = new KPIReport();
        report.setScope(dto.getScope());
        report.setMetrics(dto.getMetrics());
        report.setGeneratedDate(LocalDate.now());
        report.setStatus("ACTIVE");
        return mapToDTO(kpiReportRepository.save(report));
    }

    // Get by ID
    public KPIReportResponseDTO getById(Long id) {
        KPIReport report = kpiReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KPI report not found with ID: " + id));
        return mapToDTO(report);
    }

    // Get all
    public List<KPIReportResponseDTO> getAllReports() {
        return kpiReportRepository.findAll()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get latest by scope
    public KPIReportResponseDTO getLatestByScope(String scope) {
        KPIReport report = kpiReportRepository
                .findFirstByScopeOrderByGeneratedDateDesc(scope);
        if (report == null) throw new ResourceNotFoundException(
                "No KPI report found for scope: " + scope);
        return mapToDTO(report);
    }

    // Get trend data — reports within last X days
    public List<KPIReportResponseDTO> getTrendData(String scope, int lastXDays) {
        LocalDate startDate = LocalDate.now().minusDays(lastXDays);
        return kpiReportRepository.findByScopeAndGeneratedDateAfter(scope, startDate)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get by date range
    public List<KPIReportResponseDTO> getByDateRange(LocalDate start, LocalDate end) {
        return kpiReportRepository.findByGeneratedDateBetween(start, end)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    public void deleteReport(Long id) {
        KPIReport report = kpiReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KPI report not found with ID: " + id));
        report.setStatus("ARCHIVED");
        kpiReportRepository.save(report);
    }

    // Pagination
    public Page<KPIReportResponseDTO> getAllPagesWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return kpiReportRepository.findAll(pageable).map(this::mapToDTO);
    }


    private KPIReportResponseDTO mapToDTO(KPIReport r) {
        KPIReportResponseDTO dto = new KPIReportResponseDTO();
        dto.setReportId(r.getReportId());
        dto.setScope(r.getScope());
        dto.setMetrics(r.getMetrics());
        dto.setGeneratedDate(r.getGeneratedDate());
        dto.setStatus(r.getStatus());
        return dto;
    }
    public KPIReportResponseDTO updateReport(Long id, KPIReportRequestDTO dto) {
        KPIReport report = kpiReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KPI report not found with ID: " + id));

        report.setScope(dto.getScope());
        report.setMetrics(dto.getMetrics());

        return mapToDTO(kpiReportRepository.save(report));
    }
}