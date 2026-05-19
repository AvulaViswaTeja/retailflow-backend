package com.project.retailproject.service;

import com.project.retailproject.db.AuditLogRepository;
import com.project.retailproject.db.UserRepository;
import com.project.retailproject.dto.AuditLogRequestDTO;
import com.project.retailproject.dto.AuditLogResponseDTO;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.AuditLog;
import com.project.retailproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // AuditLogs should only be created, never updated (immutable by design)
    public AuditLogResponseDTO insertAuditLog(AuditLogRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + dto.getUserId()));

        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(dto.getAction());
        log.setTimeStamp(LocalDateTime.now());

        return mapToDTO(auditLogRepository.save(log));
    }

    public AuditLogResponseDTO getAuditLogById(Long id) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Audit log not found with ID: " + id));
        return mapToDTO(log);
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

    // --- Mapper ---
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