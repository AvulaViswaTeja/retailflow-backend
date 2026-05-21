package com.project.retailproject.service;

import com.project.retailproject.db.CatalogRepository;
import com.project.retailproject.db.ProductRepository;
import com.project.retailproject.dto.CatalogRequestDTO;
import com.project.retailproject.dto.CatalogResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Catalog;
import com.project.retailproject.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuditLogService auditLogService;

    public CatalogResponseDTO insertCatalog(CatalogRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + dto.getProductId()));

        if (dto.getExpiryDate().isBefore(dto.getEffectiveDate())) {
            auditLogService.logFailure("Catalog.CREATE",
                    "Expiry date before effective date | ProductID: " + dto.getProductId());
            throw new BadRequestException("Expiry date cannot be before effective date");
        }

        try {
            Catalog catalog = new Catalog();
            catalog.setProduct(product);
            catalog.setEffectiveDate(dto.getEffectiveDate());
            catalog.setExpiryDate(dto.getExpiryDate());
            catalog.setStatus("ACTIVE");

            CatalogResponseDTO result = mapToDTO(catalogRepository.save(catalog));
            auditLogService.log("Catalog.CREATE_SUCCESS | CatalogID: " + result.getCatalogId()
                    + " | ProductID: " + dto.getProductId()
                    + " | EffectiveDate: " + dto.getEffectiveDate()
                    + " | ExpiryDate: " + dto.getExpiryDate()
                    + " | Status: ACTIVE");
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Catalog.CREATE", ex.getMessage());
            throw ex;
        }
    }

    public CatalogResponseDTO updateCatalog(Long id, CatalogRequestDTO dto) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Catalog not found with ID: " + id));

        if (dto.getExpiryDate().isBefore(dto.getEffectiveDate())) {
            auditLogService.logFailure("Catalog.UPDATE",
                    "Expiry date before effective date | CatalogID: " + id);
            throw new BadRequestException("Expiry date cannot be before effective date");
        }

        String before = "EffectiveDate: " + catalog.getEffectiveDate()
                + " | ExpiryDate: " + catalog.getExpiryDate()
                + " | Status: " + catalog.getStatus();

        try {
            catalog.setEffectiveDate(dto.getEffectiveDate());
            catalog.setExpiryDate(dto.getExpiryDate());
            catalog.setStatus(dto.getStatus());

            CatalogResponseDTO result = mapToDTO(catalogRepository.save(catalog));
            auditLogService.log("Catalog.UPDATE_SUCCESS | CatalogID: " + id
                    + " | Before: " + before
                    + " | After: EffectiveDate: " + dto.getEffectiveDate()
                    + " | ExpiryDate: " + dto.getExpiryDate()
                    + " | Status: " + dto.getStatus());
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Catalog.UPDATE", ex.getMessage());
            throw ex;
        }
    }

    public void deleteCatalog(Long id) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Catalog not found with ID: " + id));
        try {
            catalog.setStatus("INACTIVE");
            catalogRepository.save(catalog);
            auditLogService.log("Catalog.DELETE_SUCCESS | CatalogID: " + id
                    + " | ProductID: " + catalog.getProduct().getProductId()
                    + " | Status: INACTIVE");
        } catch (Exception ex) {
            auditLogService.logFailure("Catalog.DELETE", ex.getMessage());
            throw ex;
        }
    }

    public CatalogResponseDTO getCatalogById(Long id) {
        return mapToDTO(catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Catalog not found with ID: " + id)));
    }

    public List<CatalogResponseDTO> getAllCatalogs() {
        return catalogRepository.findAll().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CatalogResponseDTO> getCatalogsByProductId(Long productId) {
        return catalogRepository.findByProductProductId(productId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Page<CatalogResponseDTO> getAllCatalogsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return catalogRepository.findAll(pageable).map(this::mapToDTO);
    }


    private CatalogResponseDTO mapToDTO(Catalog c) {
        CatalogResponseDTO dto = new CatalogResponseDTO();
        dto.setCatalogId(c.getCatalogId());
        dto.setEffectiveDate(c.getEffectiveDate());
        dto.setExpiryDate(c.getExpiryDate());
        dto.setStatus(c.getStatus());
        if (c.getProduct() != null) {
            dto.setProductId(c.getProduct().getProductId());
            dto.setProductName(c.getProduct().getProductName());
        }
        return dto;
    }
}