package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.AuditLogRequestDTO;
import com.project.retailproject.dto.AuditLogResponseDTO;
import com.project.retailproject.service.AuditLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuditLogResponseDTO>> insertAuditLog(
            @Valid @RequestBody AuditLogRequestDTO dto) {
        AuditLogResponseDTO data = auditLogService.insertAuditLog(dto);
        return ResponseEntity.ok(ApiResponse.success("Audit log created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogResponseDTO>>> getAllAuditLogs() {
        List<AuditLogResponseDTO> data = auditLogService.getAllAuditLogs();
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponseDTO>> getAuditLogById(
            @PathVariable Long id) {
        AuditLogResponseDTO data = auditLogService.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success("Audit log retrieved successfully", data));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponseDTO>>> getByUser(
            @PathVariable Long userId) {
        List<AuditLogResponseDTO> data = auditLogService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", data));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<AuditLogResponseDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<AuditLogResponseDTO> data = auditLogService.getByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", data));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<AuditLogResponseDTO>>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "auditId") String sorting,
            @RequestParam(defaultValue = "true") boolean asc) {
        Sort sort = asc ? Sort.by(sorting).ascending() : Sort.by(sorting).descending();
        Page<AuditLogResponseDTO> data = auditLogService.getAuditLogPaginated(
                PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", data));
    }
}