package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.KPIReportRequestDTO;
import com.project.retailproject.dto.KPIReportResponseDTO;
import com.project.retailproject.service.KPIReportService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<KPIReportResponseDTO>> saveReport(
            @Valid @RequestBody KPIReportRequestDTO dto) {
        KPIReportResponseDTO data = kpiReportService.saveReport(dto);
        return ResponseEntity.ok(ApiResponse.success("KPI report saved successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<KPIReportResponseDTO>>> getAllReports() {
        List<KPIReportResponseDTO> data = kpiReportService.getAllReports();
        return ResponseEntity.ok(ApiResponse.success("KPI reports retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KPIReportResponseDTO>> getById(@PathVariable Long id) {
        KPIReportResponseDTO data = kpiReportService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("KPI report retrieved successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        kpiReportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("KPI report archived successfully", null));
    }

    @GetMapping("/scope/{scope}/latest")
    public ResponseEntity<ApiResponse<KPIReportResponseDTO>> getLatestByScope(
            @PathVariable String scope) {
        KPIReportResponseDTO data = kpiReportService.getLatestByScope(scope);
        return ResponseEntity.ok(ApiResponse.success("Latest KPI report retrieved", data));
    }

    @GetMapping("/scope/{scope}/trend")
    public ResponseEntity<ApiResponse<List<KPIReportResponseDTO>>> getTrend(
            @PathVariable String scope,
            @RequestParam(defaultValue = "30") int days) {
        List<KPIReportResponseDTO> data = kpiReportService.getTrendData(scope, days);
        return ResponseEntity.ok(ApiResponse.success("Trend data retrieved successfully", data));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<KPIReportResponseDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<KPIReportResponseDTO> data = kpiReportService.getByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success("KPI reports retrieved successfully", data));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<KPIReportResponseDTO>>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<KPIReportResponseDTO> data = kpiReportService.getAllPagesWithPagination(page, size);
        return ResponseEntity.ok(ApiResponse.success("KPI reports retrieved successfully", data));
    }
}