package com.project.retailproject.controller;

import com.project.retailproject.dto.InvoiceRequestDTO;
import com.project.retailproject.dto.InvoiceResponseDTO;
import com.project.retailproject.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<InvoiceResponseDTO> insertInvoice(@RequestBody InvoiceRequestDTO dto) {
        return ResponseEntity.ok(invoiceService.insertInvoice(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(
            @PathVariable Long id, @RequestBody InvoiceRequestDTO dto) {
        return ResponseEntity.ok(invoiceService.updateInvoice(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.findInvoiceById(id));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.findAllInvoices());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoiceByStatus(@PathVariable String status) {
        return ResponseEntity.ok(invoiceService.getByStatus(status));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoiceByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(invoiceService.getByDateRange(start, end));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<InvoiceResponseDTO>> getInvoicePaginated(Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getAllInvoicesPaginated(pageable));
    }
}