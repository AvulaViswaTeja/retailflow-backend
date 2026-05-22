package com.project.retailproject.controller;

import com.project.retailproject.dto.KPIReportRequestDTO;
import com.project.retailproject.dto.KPIReportResponseDTO;
import com.project.retailproject.service.KPIReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/kpi-reports")
public class KPIReportController {

    @Autowired
    private KPIReportService kpiReportService;

    @PostMapping
    public ResponseEntity<KPIReportResponseDTO> saveReport(
            @RequestBody KPIReportRequestDTO dto) {
        return ResponseEntity.ok(kpiReportService.saveReport(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KPIReportResponseDTO> updateReport(
            @PathVariable Long id,
            @RequestBody KPIReportRequestDTO dto) {
        return ResponseEntity.ok(kpiReportService.updateReport(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveReport(
            @PathVariable Long id) {
        kpiReportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KPIReportResponseDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(kpiReportService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<KPIReportResponseDTO>> getAll() {
        return ResponseEntity.ok(kpiReportService.getAllReports());
    }

    @GetMapping("/scope/{scope}/latest")
    public ResponseEntity<KPIReportResponseDTO> getLatestByScope(
            @PathVariable String scope) {
        return ResponseEntity.ok(kpiReportService.getLatestByScope(scope));
    }

    @GetMapping("/scope/{scope}/trend")
    public ResponseEntity<List<KPIReportResponseDTO>> getTrend(
            @PathVariable String scope,
            @RequestParam(defaultValue = "30") int lastXDays) {
        return ResponseEntity.ok(kpiReportService.getTrendData(scope, lastXDays));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<KPIReportResponseDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(kpiReportService.getByDateRange(start, end));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<KPIReportResponseDTO>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(kpiReportService.getAllPagesWithPagination(page, size));
    }
}