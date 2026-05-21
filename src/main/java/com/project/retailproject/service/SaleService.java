package com.project.retailproject.service;

import com.project.retailproject.db.*;
import com.project.retailproject.dto.SaleRequestDTO;
import com.project.retailproject.dto.SaleResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public SaleResponseDTO insertSale(SaleRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + dto.getProductId()));

        // Check 1 — product must be ACTIVE
        if (!product.getStatus().equalsIgnoreCase("ACTIVE")) {
            auditLogService.logFailure("Sale.CREATE",
                    "Attempt to sell INACTIVE ProductID: " + dto.getProductId());
            throw new BadRequestException("Cannot sell an inactive product");
        }

        // Check 2 — product must have an ACTIVE catalog entry valid for today
        Catalog activeCatalog = catalogRepository
                .findFirstByProductAndStatusAndEffectiveDateLessThanEqualAndExpiryDateGreaterThanEqual(
                        product, "ACTIVE", LocalDate.now(), LocalDate.now())
                .orElseThrow(() -> {
                    auditLogService.logFailure("Sale.CREATE",
                            "No active catalog listing for ProductID: " + dto.getProductId()
                                    + " on date: " + LocalDate.now());
                    return new BadRequestException(
                            "Product has no active catalog listing for today");
                });

        try {
            double amount = product.getPrice() * dto.getQuantity();

            Sale sale = new Sale();
            sale.setProduct(product);
            sale.setCustomerId(dto.getCustomerId());
            sale.setQuantity(dto.getQuantity());
            sale.setAmount(amount);
            sale.setDate(LocalDate.now());
            sale.setStatus(dto.getStatus() != null ? dto.getStatus() : "COMPLETED");

            Sale savedSale = saleRepository.save(sale);

            auditLogService.log("Sale.CREATE_SUCCESS | SaleID: " + savedSale.getSaleId()
                    + " | ProductID: " + dto.getProductId()
                    + " | CatalogID: " + activeCatalog.getCatalogId()
                    + " | CustomerID: " + dto.getCustomerId()
                    + " | Qty: " + dto.getQuantity()
                    + " | Amount: " + amount
                    + " | Status: " + savedSale.getStatus());

            // Auto-generate invoice for COMPLETED sales
            Invoice invoice = null;
            if (savedSale.getStatus().equalsIgnoreCase("COMPLETED")) {
                invoice = new Invoice();
                invoice.setSale(savedSale);
                invoice.setAmount(savedSale.getAmount());
                invoice.setDate(LocalDate.now());
                invoice.setStatus("PENDING");
                invoiceRepository.save(invoice);

                auditLogService.log("Invoice.AUTO_GENERATED | InvoiceID: " + invoice.getInvoiceId()
                        + " | SaleID: " + savedSale.getSaleId()
                        + " | Amount: " + invoice.getAmount()
                        + " | Status: PENDING");
            }

            return mapToDTO(savedSale, invoice);
        } catch (Exception ex) {
            auditLogService.logFailure("Sale.CREATE", ex.getMessage());
            throw ex;
        }
    }

    public SaleResponseDTO updateSale(Long id, SaleRequestDTO dto) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found with ID: " + id));

        if (sale.getStatus().equalsIgnoreCase("CANCELLED")) {
            auditLogService.logFailure("Sale.UPDATE",
                    "Attempt to update CANCELLED SaleID: " + id);
            throw new BadRequestException("Cannot update a cancelled sale");
        }

        String before = "Qty: " + sale.getQuantity()
                + " | Status: " + sale.getStatus();

        try {
            sale.setQuantity(dto.getQuantity());
            sale.setStatus(dto.getStatus());
            SaleResponseDTO result = mapToDTO(saleRepository.save(sale), null);
            auditLogService.log("Sale.UPDATE_SUCCESS | SaleID: " + id
                    + " | Before: " + before
                    + " | After: Qty: " + dto.getQuantity()
                    + " | Status: " + dto.getStatus());
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Sale.UPDATE", ex.getMessage());
            throw ex;
        }
    }

    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found with ID: " + id));
        try {
            sale.setStatus("CANCELLED");
            saleRepository.save(sale);
            auditLogService.log("Sale.CANCEL_SUCCESS | SaleID: " + id
                    + " | CustomerID: " + sale.getCustomerId()
                    + " | Amount: " + sale.getAmount()
                    + " | Status: CANCELLED");
        } catch (Exception ex) {
            auditLogService.logFailure("Sale.CANCEL", ex.getMessage());
            throw ex;
        }
    }

    public SaleResponseDTO getSaleById(Long id) {
        return mapToDTO(saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found with ID: " + id)), null);
    }

    public List<SaleResponseDTO> getAllSales() {
        return saleRepository.findAll().stream()
                .map(s -> mapToDTO(s, null)).collect(Collectors.toList());
    }

    public List<SaleResponseDTO> getSalesByCustomer(Long customerId) {
        return saleRepository.findByCustomerId(customerId)
                .stream().map(s -> mapToDTO(s, null)).collect(Collectors.toList());
    }

    public List<SaleResponseDTO> getSalesByDateRange(LocalDate start, LocalDate end) {
        return saleRepository.findByDateBetween(start, end)
                .stream().map(s -> mapToDTO(s, null)).collect(Collectors.toList());
    }

    public Page<SaleResponseDTO> getAllSalesPaginated(Pageable pageable) {
        return saleRepository.findAll(pageable).map(s -> mapToDTO(s, null));
    }


    private SaleResponseDTO mapToDTO(Sale s, Invoice invoice) {
        SaleResponseDTO dto = new SaleResponseDTO();
        dto.setSaleId(s.getSaleId());
        dto.setCustomerId(s.getCustomerId());
        dto.setQuantity(s.getQuantity());
        dto.setAmount(s.getAmount());
        dto.setDate(s.getDate());
        dto.setStatus(s.getStatus());
        if (s.getProduct() != null) {
            dto.setProductId(s.getProduct().getProductId());
            dto.setProductName(s.getProduct().getProductName());
        }
        if (invoice != null) {
            dto.setInvoiceId(invoice.getInvoiceId());
        }
        return dto;
    }
}