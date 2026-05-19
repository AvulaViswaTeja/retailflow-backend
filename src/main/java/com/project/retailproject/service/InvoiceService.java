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
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private SaleRepository saleRepository;

    public InvoiceResponseDTO insertInvoice(InvoiceRequestDTO dto) {
        Sale sale = saleRepository.findById(dto.getSaleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found with ID: " + dto.getSaleId()));

        // One invoice per sale
        invoiceRepository.findBySale_SaleId(dto.getSaleId()).ifPresent(i -> {
            throw new BadRequestException("Invoice already exists for sale ID: " + dto.getSaleId());
        });

        Invoice invoice = new Invoice();
        invoice.setSale(sale);
        invoice.setAmount(dto.getAmount());
        invoice.setDate(LocalDate.now());
        invoice.setStatus("PENDING");

        return mapToDTO(invoiceRepository.save(invoice));
    }

    public InvoiceResponseDTO updateInvoice(Long id, InvoiceRequestDTO dto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + id));

        if (invoice.getStatus().equals("PAID")) {
            throw new BadRequestException("Cannot update a paid invoice");
        }

        invoice.setAmount(dto.getAmount());
        invoice.setStatus(dto.getStatus());

        return mapToDTO(invoiceRepository.save(invoice));
    }

    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + id));

        if (invoice.getStatus().equals("PAID")) {
            throw new BadRequestException("Cannot cancel a paid invoice");
        }

        invoice.setStatus("CANCELLED");
        invoiceRepository.save(invoice);
    }

    public InvoiceResponseDTO findInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + id));
        return mapToDTO(invoice);
    }

    public List<InvoiceResponseDTO> findAllInvoices() {
        return invoiceRepository.findAll()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<InvoiceResponseDTO> getByStatus(String status) {
        return invoiceRepository.findByStatus(status)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<InvoiceResponseDTO> getByDateRange(LocalDate start, LocalDate end) {
        return invoiceRepository.findByDateBetween(start, end)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<InvoiceResponseDTO> getAllInvoicesPaginated(Pageable pageable) {
        return invoiceRepository.findAll(pageable).map(this::mapToDTO);
    }

    // --- Mapper ---
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
}