package com.project.retailproject.service;

import com.project.retailproject.db.InventoryRepository;
import com.project.retailproject.db.KPIReportRepository;
import com.project.retailproject.db.SaleRepository;
import com.project.retailproject.dto.KPIReportRequestDTO;
import com.project.retailproject.dto.KPIReportResponseDTO;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.KPIReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KPIReportService {

    @Autowired
    private KPIReportRepository kpiReportRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private static final double SHRINKAGE_THRESHOLD = 5.0;
    private static final double MIN_STOCK_TURNOVER  = 2.0;
    private static final double MIN_SALES_GROWTH    = 0.0;


    // ── SAVE — computes KPIs from real data ───────────────────────────────────
    public KPIReportResponseDTO saveReport(KPIReportRequestDTO dto) {

        // Step 1 — Query Sale table for both periods
        Double currentSales  = saleRepository.getSalesInPeriod(
                dto.getCurrentStart(), dto.getCurrentEnd());
        Double previousSales = saleRepository.getSalesInPeriod(
                dto.getPreviousStart(), dto.getPreviousEnd());

        // Step 2 — Compute Sales Growth
        // formula: (current - previous) / previous x 100
        Double salesGrowth = (previousSales != null && previousSales > 0)
                ? ((currentSales - previousSales) / previousSales) * 100
                : 0.0;

        // Step 3 — Compute Stock Turnover
        // formula: total units sold / average inventory on hand
        Double totalUnitsSold = saleRepository.getTotalUnitsSoldInPeriod(
                dto.getCurrentStart(), dto.getCurrentEnd());
        Double avgInventory   = inventoryRepository.getAverageInventory();
        Double stockTurnover  = (avgInventory != null && avgInventory > 0)
                ? totalUnitsSold / avgInventory
                : 0.0;

        // Step 4 — Compute Shrinkage Rate
        // formula: (safetyStock - quantityOnHand) / safetyStock x 100
        Double recorded      = inventoryRepository.getRecordedInventory();
        Double actual        = inventoryRepository.getActualInventory();
        Double shrinkageRate = (recorded != null && recorded > 0 && recorded > actual)
                ? ((recorded - actual) / recorded) * 100
                : 0.0;


        stockTurnover = round(stockTurnover);
        salesGrowth   = round(salesGrowth);
        shrinkageRate = round(shrinkageRate);


        String metrics = String.format(
                "Stock Turnover: %.2f | Sales Growth: %.1f%% | Shrinkage: %.1f%%",
                stockTurnover, salesGrowth, shrinkageRate);


        KPIReport report = new KPIReport();
        report.setScope(dto.getScope());
        report.setMetrics(metrics);
        report.setGeneratedDate(LocalDate.now());
        report.setStatus("ACTIVE");
        report.setStockTurnover(stockTurnover);
        report.setSalesGrowth(salesGrowth);
        report.setShrinkageRate(shrinkageRate);
        report.setCurrentStart(dto.getCurrentStart());
        report.setCurrentEnd(dto.getCurrentEnd());
        report.setPreviousStart(dto.getPreviousStart());
        report.setPreviousEnd(dto.getPreviousEnd());

        return mapToDTO(kpiReportRepository.save(report));
    }



    public KPIReportResponseDTO getById(Long id) {
        KPIReport report = kpiReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KPI report not found with ID: " + id));
        return mapToDTO(report);
    }



    public List<KPIReportResponseDTO> getAllReports() {
        return kpiReportRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }



    public KPIReportResponseDTO getLatestByScope(String scope) {
        KPIReport report = kpiReportRepository
                .findFirstByScopeOrderByGeneratedDateDesc(scope);
        if (report == null) {
            throw new ResourceNotFoundException(
                    "No KPI report found for scope: " + scope);
        }
        return mapToDTO(report);
    }



    public List<KPIReportResponseDTO> getTrendData(String scope, int lastXDays) {
        LocalDate startDate = LocalDate.now().minusDays(lastXDays);
        return kpiReportRepository
                .findByScopeAndGeneratedDateAfter(scope, startDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }



    public List<KPIReportResponseDTO> getByDateRange(
            LocalDate start, LocalDate end) {
        return kpiReportRepository
                .findByGeneratedDateBetween(start, end)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }



    public void deleteReport(Long id) {
        KPIReport report = kpiReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KPI report not found with ID: " + id));
        report.setStatus("ARCHIVED");
        kpiReportRepository.save(report);
    }



    public KPIReportResponseDTO updateReport(Long id, KPIReportRequestDTO dto) {
        KPIReport report = kpiReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KPI report not found with ID: " + id));

        report.setScope(dto.getScope());

        if (dto.getCurrentStart() != null && dto.getCurrentEnd() != null
                && dto.getPreviousStart() != null && dto.getPreviousEnd() != null) {

            Double currentSales  = saleRepository.getSalesInPeriod(
                    dto.getCurrentStart(), dto.getCurrentEnd());
            Double previousSales = saleRepository.getSalesInPeriod(
                    dto.getPreviousStart(), dto.getPreviousEnd());

            Double salesGrowth = (previousSales != null && previousSales > 0)
                    ? ((currentSales - previousSales) / previousSales) * 100
                    : 0.0;

            Double totalUnitsSold = saleRepository.getTotalUnitsSoldInPeriod(
                    dto.getCurrentStart(), dto.getCurrentEnd());
            Double avgInventory   = inventoryRepository.getAverageInventory();
            Double stockTurnover  = (avgInventory != null && avgInventory > 0)
                    ? totalUnitsSold / avgInventory
                    : 0.0;

            Double recorded      = inventoryRepository.getRecordedInventory();
            Double actual        = inventoryRepository.getActualInventory();
            Double shrinkageRate = (recorded != null && recorded > 0 && recorded > actual)
                    ? ((recorded - actual) / recorded) * 100
                    : 0.0;

            stockTurnover = round(stockTurnover);
            salesGrowth   = round(salesGrowth);
            shrinkageRate = round(shrinkageRate);

            String metrics = String.format(
                    "Stock Turnover: %.2f | Sales Growth: %.1f%% | Shrinkage: %.1f%%",
                    stockTurnover, salesGrowth, shrinkageRate);

            report.setMetrics(metrics);
            report.setStockTurnover(stockTurnover);
            report.setSalesGrowth(salesGrowth);
            report.setShrinkageRate(shrinkageRate);
            report.setCurrentStart(dto.getCurrentStart());
            report.setCurrentEnd(dto.getCurrentEnd());
            report.setPreviousStart(dto.getPreviousStart());
            report.setPreviousEnd(dto.getPreviousEnd());

        } else if (dto.getMetrics() != null) {
            report.setMetrics(dto.getMetrics());
        }

        return mapToDTO(kpiReportRepository.save(report));
    }



    public Page<KPIReportResponseDTO> getAllPagesWithPagination(
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return kpiReportRepository.findAll(pageable)
                .map(this::mapToDTO);
    }



    private KPIReportResponseDTO mapToDTO(KPIReport r) {
        KPIReportResponseDTO dto = new KPIReportResponseDTO();
        dto.setReportId(r.getReportId());
        dto.setScope(r.getScope());
        dto.setMetrics(r.getMetrics());
        dto.setGeneratedDate(r.getGeneratedDate());
        dto.setStatus(r.getStatus());
        dto.setStockTurnover(r.getStockTurnover());
        dto.setSalesGrowth(r.getSalesGrowth());
        dto.setShrinkageRate(r.getShrinkageRate());
        dto.setCurrentStart(r.getCurrentStart());
        dto.setCurrentEnd(r.getCurrentEnd());
        dto.setPreviousStart(r.getPreviousStart());
        dto.setPreviousEnd(r.getPreviousEnd());
        return dto;
    }



    private Double round(Double value) {
        if (value == null) return 0.0;
        return Math.round(value * 100.0) / 100.0;
    }
}