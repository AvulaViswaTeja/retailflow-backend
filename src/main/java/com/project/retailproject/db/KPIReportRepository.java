package com.project.retailproject.db;

import com.project.retailproject.model.KPIReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KPIReportRepository extends JpaRepository<KPIReport, Long> {
    List<KPIReport> findByScope(String scope);
    List<KPIReport> findByStatus(String status);
    KPIReport findFirstByScopeOrderByGeneratedDateDesc(String scope);
    List<KPIReport> findByScopeAndGeneratedDateAfter(String scope, LocalDate date);
    List<KPIReport> findByGeneratedDateBetween(LocalDate start, LocalDate end);
}