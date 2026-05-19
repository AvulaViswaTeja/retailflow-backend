package com.project.retailproject.db;

import com.project.retailproject.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserUserId(Long userId);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByTimeStampBetween(LocalDateTime start, LocalDateTime end);
}