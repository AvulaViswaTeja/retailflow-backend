package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.SaleRequestDTO;
import com.project.retailproject.dto.SaleResponseDTO;
import com.project.retailproject.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @PostMapping
    public ResponseEntity<ApiResponse<SaleResponseDTO>> insertSale(
            @Valid @RequestBody SaleRequestDTO dto) {
        SaleResponseDTO data = saleService.insertSale(dto);
        return ResponseEntity.ok(ApiResponse.success("Sale created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleResponseDTO>>> getAllSales() {
        List<SaleResponseDTO> data = saleService.getAllSales();
        return ResponseEntity.ok(ApiResponse.success("Sales retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> getSaleById(@PathVariable Long id) {
        SaleResponseDTO data = saleService.getSaleById(id);
        return ResponseEntity.ok(ApiResponse.success("Sale retrieved successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleResponseDTO>> updateSale(
            @PathVariable Long id,
            @Valid @RequestBody SaleRequestDTO dto) {
        SaleResponseDTO data = saleService.updateSale(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Sale updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return ResponseEntity.ok(ApiResponse.success("Sale cancelled successfully", null));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<SaleResponseDTO>>> getSalesByCustomer(
            @PathVariable Long customerId) {
        List<SaleResponseDTO> data = saleService.getSalesByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success("Sales retrieved successfully", data));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<SaleResponseDTO>>> getSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<SaleResponseDTO> data = saleService.getSalesByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success("Sales retrieved successfully", data));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<SaleResponseDTO>>> getAllSalesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "saleId") String sorting,
            @RequestParam(defaultValue = "true") boolean asc) {
        Sort sort = asc ? Sort.by(sorting).ascending() : Sort.by(sorting).descending();
        Page<SaleResponseDTO> data = saleService.getAllSalesPaginated(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success("Sales retrieved successfully", data));
    }
}