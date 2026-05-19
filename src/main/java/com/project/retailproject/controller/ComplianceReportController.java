package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.ComplianceReportRequestDTO;
import com.project.retailproject.dto.ComplianceReportResponseDTO;
import com.project.retailproject.service.ComplianceReportService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<ComplianceReportResponseDTO>> insertReport(
            @Valid @RequestBody ComplianceReportRequestDTO dto) {
        ComplianceReportResponseDTO data = complianceReportService.insertComplianceReport(dto);
        return ResponseEntity.ok(ApiResponse.success("Compliance report created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplianceReportResponseDTO>>> getAllReports() {
        List<ComplianceReportResponseDTO> data = complianceReportService.getAllComplianceReports();
        return ResponseEntity.ok(ApiResponse.success("Compliance reports retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplianceReportResponseDTO>> getReportById(
            @PathVariable Long id) {
        ComplianceReportResponseDTO data = complianceReportService.getComplianceReport(id);
        return ResponseEntity.ok(ApiResponse.success("Compliance report retrieved successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplianceReportResponseDTO>> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ComplianceReportRequestDTO dto) {
        ComplianceReportResponseDTO data = complianceReportService.updateComplianceReport(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Compliance report updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        complianceReportService.deleteComplianceReport(id);
        return ResponseEntity.ok(ApiResponse.success("Compliance report archived successfully", null));
    }

    @GetMapping("/scope/{scope}")
    public ResponseEntity<ApiResponse<List<ComplianceReportResponseDTO>>> getByScope(
            @PathVariable String scope) {
        List<ComplianceReportResponseDTO> data = complianceReportService.getByScope(scope);
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", data));
    }

    @GetMapping("/scope/{scope}/latest")
    public ResponseEntity<ApiResponse<ComplianceReportResponseDTO>> getLatestByScope(
            @PathVariable String scope) {
        ComplianceReportResponseDTO data = complianceReportService.getLatestByScope(scope);
        return ResponseEntity.ok(ApiResponse.success("Latest report retrieved successfully", data));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<ComplianceReportResponseDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<ComplianceReportResponseDTO> data = complianceReportService.getByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", data));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<ComplianceReportResponseDTO>>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ComplianceReportResponseDTO> data = complianceReportService
                .getAllPagesWithPagination(page, size);
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", data));
    }
}