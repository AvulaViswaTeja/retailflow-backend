package com.project.retailproject.service;

import com.project.retailproject.db.InvoiceRepository;
import com.project.retailproject.db.SaleRepository;
import com.project.retailproject.dto.InvoiceRequestDTO;
import com.project.retailproject.dto.InvoiceResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Invoice;
import com.project.retailproject.model.Sale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private AuditLogService auditLogService;

    public InvoiceResponseDTO insertInvoice(InvoiceRequestDTO dto) {
        Sale sale = saleRepository.findById(dto.getSaleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found with ID: " + dto.getSaleId()));

        invoiceRepository.findBySale_SaleId(dto.getSaleId()).ifPresent(i -> {
            auditLogService.logFailure("Invoice.CREATE",
                    "Duplicate invoice for SaleID: " + dto.getSaleId());
            throw new BadRequestException(
                    "Invoice already exists for sale ID: " + dto.getSaleId());
        });

        try {
            Invoice invoice = new Invoice();
            invoice.setSale(sale);
            invoice.setAmount(dto.getAmount());
            invoice.setDate(LocalDate.now());
            invoice.setStatus("PENDING");

            InvoiceResponseDTO result = mapToDTO(invoiceRepository.save(invoice));
            auditLogService.log("Invoice.CREATE_SUCCESS | InvoiceID: " + result.getInvoiceId()
                    + " | SaleID: " + dto.getSaleId()
                    + " | Amount: " + dto.getAmount()
                    + " | Status: PENDING");
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Invoice.CREATE", ex.getMessage());
            throw ex;
        }
    }

    public InvoiceResponseDTO updateInvoice(Long id, InvoiceRequestDTO dto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + id));

        if (invoice.getStatus().equals("PAID")) {
            auditLogService.logFailure("Invoice.UPDATE",
                    "Attempt to update PAID InvoiceID: " + id);
            throw new BadRequestException("Cannot update a paid invoice");
        }

        String before = "Amount: " + invoice.getAmount()
                + " | Status: " + invoice.getStatus();

        try {
            invoice.setAmount(dto.getAmount());
            invoice.setStatus(dto.getStatus());

            InvoiceResponseDTO result = mapToDTO(invoiceRepository.save(invoice));
            auditLogService.log("Invoice.UPDATE_SUCCESS | InvoiceID: " + id
                    + " | Before: " + before
                    + " | After: Amount: " + dto.getAmount()
                    + " | Status: " + dto.getStatus());
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Invoice.UPDATE", ex.getMessage());
            throw ex;
        }
    }

    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + id));

        if (invoice.getStatus().equals("PAID")) {
            auditLogService.logFailure("Invoice.CANCEL",
                    "Attempt to cancel PAID InvoiceID: " + id);
            throw new BadRequestException("Cannot cancel a paid invoice");
        }

        try {
            invoice.setStatus("CANCELLED");
            invoiceRepository.save(invoice);
            auditLogService.log("Invoice.CANCEL_SUCCESS | InvoiceID: " + id
                    + " | SaleID: " + invoice.getSale().getSaleId()
                    + " | Amount: " + invoice.getAmount()
                    + " | Status: CANCELLED");
        } catch (Exception ex) {
            auditLogService.logFailure("Invoice.CANCEL", ex.getMessage());
            throw ex;
        }
    }

    public InvoiceResponseDTO findInvoiceById(Long id) {
        return mapToDTO(invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + id)));
    }

    public List<InvoiceResponseDTO> findAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<InvoiceResponseDTO> getByStatus(String status) {
        return invoiceRepository.findByStatus(status)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<InvoiceResponseDTO> getByDateRange(LocalDate start, LocalDate end) {
        return invoiceRepository.findByDateBetween(start, end)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Page<InvoiceResponseDTO> getAllInvoicesPaginated(Pageable pageable) {
        return invoiceRepository.findAll(pageable).map(this::mapToDTO);
    }


    private InvoiceResponseDTO mapToDTO(Invoice i) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setInvoiceId(i.getInvoiceId());
        dto.setAmount(i.getAmount());
        dto.setDate(i.getDate());
        dto.setStatus(i.getStatus());
        if (i.getSale() != null) {
            dto.setSaleId(i.getSale().getSaleId());
            dto.setCustomerId(i.getSale().getCustomerId());
        }
        return dto;
    }

    public InvoiceResponseDTO getInvoiceBySaleId(Long saleId) {
        Optional<Invoice> invoice  = invoiceRepository.findBySale_SaleId(saleId);
        if(invoice.isPresent()) {
            Invoice inv = invoice.get();

            InvoiceResponseDTO dto = new InvoiceResponseDTO();
            dto.setInvoiceId(inv.getInvoiceId());
            dto.setAmount(inv.getAmount());
            dto.setDate(inv.getDate());
            dto.setStatus(inv.getStatus());
            if (inv.getSale() != null) {
                dto.setSaleId(inv.getSale().getSaleId());
                dto.setCustomerId(inv.getSale().getCustomerId());
            }

            return dto;
        }

        return null;

    }
}