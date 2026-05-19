package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.InvoiceRequestDTO;
import com.project.retailproject.dto.InvoiceResponseDTO;
import com.project.retailproject.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> insertInvoice(
            @Valid @RequestBody InvoiceRequestDTO dto) {
        InvoiceResponseDTO data = invoiceService.insertInvoice(dto);
        return ResponseEntity.ok(ApiResponse.success("Invoice created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponseDTO>>> getAllInvoices() {
        List<InvoiceResponseDTO> data = invoiceService.findAllInvoices();
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> getInvoiceById(@PathVariable Long id) {
        InvoiceResponseDTO data = invoiceService.findInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice retrieved successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequestDTO dto) {
        InvoiceResponseDTO data = invoiceService.updateInvoice(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Invoice updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice cancelled successfully", null));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<InvoiceResponseDTO>>> getByStatus(
            @PathVariable String status) {
        List<InvoiceResponseDTO> data = invoiceService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved successfully", data));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<InvoiceResponseDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<InvoiceResponseDTO> data = invoiceService.getByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved successfully", data));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<InvoiceResponseDTO>>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceId") String sorting,
            @RequestParam(defaultValue = "true") boolean asc) {
        Sort sort = asc ? Sort.by(sorting).ascending() : Sort.by(sorting).descending();
        Page<InvoiceResponseDTO> data = invoiceService.getAllInvoicesPaginated(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved successfully", data));
    }
}