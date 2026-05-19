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

    // Add catalog
    public CatalogResponseDTO insertCatalog(CatalogRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + dto.getProductId()));

        // Validate dates
        if (dto.getExpiryDate().isBefore(dto.getEffectiveDate())) {
            throw new BadRequestException("Expiry date cannot be before effective date");
        }

        Catalog catalog = new Catalog();
        catalog.setProduct(product);
        catalog.setEffectiveDate(dto.getEffectiveDate());
        catalog.setExpiryDate(dto.getExpiryDate());
        catalog.setStatus("ACTIVE");

        return mapToDTO(catalogRepository.save(catalog));
    }

    // Update catalog
    public CatalogResponseDTO updateCatalog(Long id, CatalogRequestDTO dto) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Catalog not found with ID: " + id));

        if (dto.getExpiryDate().isBefore(dto.getEffectiveDate())) {
            throw new BadRequestException("Expiry date cannot be before effective date");
        }

        catalog.setEffectiveDate(dto.getEffectiveDate());
        catalog.setExpiryDate(dto.getExpiryDate());
        catalog.setStatus(dto.getStatus());

        return mapToDTO(catalogRepository.save(catalog));
    }

    // Soft delete
    public void deleteCatalog(Long id) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Catalog not found with ID: " + id));
        catalog.setStatus("INACTIVE");
        catalogRepository.save(catalog);
    }

    // Get by ID
    public CatalogResponseDTO getCatalogById(Long id) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Catalog not found with ID: " + id));
        return mapToDTO(catalog);
    }

    // Get all
    public List<CatalogResponseDTO> getAllCatalogs() {
        return catalogRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get by product ID
    public List<CatalogResponseDTO> getCatalogsByProductId(Long productId) {
        return catalogRepository.findByProductProductId(productId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Pagination
    public Page<CatalogResponseDTO> getAllCatalogsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return catalogRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    // --- Mapper ---
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