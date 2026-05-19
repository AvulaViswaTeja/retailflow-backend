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

    @Transactional
    public SaleResponseDTO insertSale(SaleRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + dto.getProductId()));

        if (!product.getStatus().equalsIgnoreCase("ACTIVE")) {
            throw new BadRequestException("Cannot sell an inactive product");
        }

        // Auto-calculate amount from product price
        double amount = product.getPrice() * dto.getQuantity();

        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setCustomerId(dto.getCustomerId());
        sale.setQuantity(dto.getQuantity());
        sale.setAmount(amount);
        sale.setDate(LocalDate.now());
        sale.setStatus(dto.getStatus() != null ? dto.getStatus() : "COMPLETED");

        Sale savedSale = saleRepository.save(sale);

        // Auto-generate invoice when sale is COMPLETED
        Invoice invoice = null;
        if (savedSale.getStatus().equalsIgnoreCase("COMPLETED")) {
            invoice = new Invoice();
            invoice.setSale(savedSale);
            invoice.setAmount(savedSale.getAmount());
            invoice.setDate(LocalDate.now());
            invoice.setStatus("PENDING");
            invoiceRepository.save(invoice);
        }

        return mapToDTO(savedSale, invoice);
    }

    public SaleResponseDTO updateSale(Long id, SaleRequestDTO dto) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found with ID: " + id));

        if (sale.getStatus().equalsIgnoreCase("CANCELLED")) {
            throw new BadRequestException("Cannot update a cancelled sale");
        }

        sale.setQuantity(dto.getQuantity());
        sale.setStatus(dto.getStatus());

        return mapToDTO(saleRepository.save(sale), null);
    }

    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found with ID: " + id));
        sale.setStatus("CANCELLED");
        saleRepository.save(sale);
    }

    public SaleResponseDTO getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found with ID: " + id));
        return mapToDTO(sale, null);
    }

    public List<SaleResponseDTO> getAllSales() {
        return saleRepository.findAll()
                .stream().map(s -> mapToDTO(s, null))
                .collect(Collectors.toList());
    }

    public List<SaleResponseDTO> getSalesByCustomer(Long customerId) {
        return saleRepository.findByCustomerId(customerId)
                .stream().map(s -> mapToDTO(s, null))
                .collect(Collectors.toList());
    }

    public List<SaleResponseDTO> getSalesByDateRange(LocalDate start, LocalDate end) {
        return saleRepository.findByDateBetween(start, end)
                .stream().map(s -> mapToDTO(s, null))
                .collect(Collectors.toList());
    }

    public Page<SaleResponseDTO> getAllSalesPaginated(Pageable pageable) {
        return saleRepository.findAll(pageable)
                .map(s -> mapToDTO(s, null));
    }

    // --- Mapper ---
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