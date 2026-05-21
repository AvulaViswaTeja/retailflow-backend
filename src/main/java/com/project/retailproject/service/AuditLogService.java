package com.project.retailproject.service;

import com.project.retailproject.db.AuditLogRepository;
import com.project.retailproject.db.UserRepository;
import com.project.retailproject.dto.AuditLogResponseDTO;
import com.project.retailproject.model.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;


    public void log(String action) {
        log(action, getCurrentUsername());
    }


    public void log(String action, String performedByEmail) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setTimeStamp(LocalDateTime.now());
        userRepository.findByEmail(performedByEmail).ifPresent(log::setUser);
        auditLogRepository.save(log);
    }


    public void logFailure(String action, String errorMessage) {
        log(action + "_FAILED | Error: " + errorMessage);
    }

    public void logFailure(String action, String errorMessage, String performedByEmail) {
        log(action + "_FAILED | Error: " + errorMessage, performedByEmail);
    }


    public AuditLogResponseDTO getAuditLogById(Long id) {
        AuditLog logEntry = auditLogRepository.findById(id)
                .orElseThrow(() -> new com.project.retailproject.exception
                        .ResourceNotFoundException("Audit log not found with ID: " + id));
        return mapToDTO(logEntry);
    }

    public List<AuditLogResponseDTO> getAllAuditLogs() {
        return auditLogRepository.findAll()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponseDTO> getByUserId(Long userId) {
        return auditLogRepository.findByUserUserId(userId)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponseDTO> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimeStampBetween(start, end)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<AuditLogResponseDTO> getAuditLogPaginated(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::mapToDTO);
    }



    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";
    }

    private AuditLogResponseDTO mapToDTO(AuditLog a) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        dto.setAuditId(a.getAuditId());
        dto.setAction(a.getAction());
        dto.setTimeStamp(a.getTimeStamp());
        if (a.getUser() != null) {
            dto.setUserId(a.getUser().getUserId());
            dto.setUserName(a.getUser().getUserName());
        }
        return dto;
    }
}