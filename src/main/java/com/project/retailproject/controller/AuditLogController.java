package com.project.retailproject.controller;

import com.project.retailproject.dto.AuditLogResponseDTO;
import com.project.retailproject.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getAuditLogById(id));
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponseDTO>> getAll() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponseDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogService.getByUserId(userId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLogResponseDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(auditLogService.getByDateRange(start, end));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<AuditLogResponseDTO>> getPaginated(Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAuditLogPaginated(pageable));
    }
}