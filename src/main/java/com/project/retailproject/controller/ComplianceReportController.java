package com.project.retailproject.controller;

import com.project.retailproject.dto.ComplianceReportRequestDTO;
import com.project.retailproject.dto.ComplianceReportResponseDTO;
import com.project.retailproject.service.ComplianceReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/compliance-reports")
public class ComplianceReportController {

    @Autowired
    private ComplianceReportService complianceReportService;

    @PostMapping
    public ResponseEntity<ComplianceReportResponseDTO> generate(
            @RequestBody ComplianceReportRequestDTO dto) {
        return ResponseEntity.ok(complianceReportService.insertComplianceReport(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComplianceReportResponseDTO> update(
            @PathVariable Long id, @RequestBody ComplianceReportRequestDTO dto) {
        return ResponseEntity.ok(complianceReportService.updateComplianceReport(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable Long id) {
        complianceReportService.deleteComplianceReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplianceReportResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(complianceReportService.getComplianceReport(id));
    }

    @GetMapping
    public ResponseEntity<List<ComplianceReportResponseDTO>> getAll() {
        return ResponseEntity.ok(complianceReportService.getAllComplianceReports());
    }

    @GetMapping("/scope/{scope}")
    public ResponseEntity<List<ComplianceReportResponseDTO>> getByScope(
            @PathVariable String scope) {
        return ResponseEntity.ok(complianceReportService.getByScope(scope));
    }

    @GetMapping("/scope/{scope}/latest")
    public ResponseEntity<ComplianceReportResponseDTO> getLatestByScope(
            @PathVariable String scope) {
        return ResponseEntity.ok(complianceReportService.getLatestByScope(scope));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ComplianceReportResponseDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(complianceReportService.getByDateRange(start, end));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ComplianceReportResponseDTO>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(complianceReportService.getAllPagesWithPagination(page, size));
    }
}