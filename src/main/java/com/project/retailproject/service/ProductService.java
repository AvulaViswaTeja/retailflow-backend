package com.project.retailproject.service;

import com.project.retailproject.db.CatalogRepository;
import com.project.retailproject.db.ProductRepository;
import com.project.retailproject.dto.ProductRequestDTO;
import com.project.retailproject.dto.ProductResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Catalog;
import com.project.retailproject.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private AuditLogService auditLogService;

    public ProductResponseDTO addProduct(ProductRequestDTO dto) {
        if (productRepository.existsByProductName(dto.getProductName())) {
            auditLogService.logFailure("Product.CREATE",
                    "Duplicate product name: " + dto.getProductName());
            throw new BadRequestException(
                    "Product with name '" + dto.getProductName() + "' already exists");
        }
        try {
            Product product = mapToEntity(dto);
            product.setStatus("ACTIVE");
            ProductResponseDTO result = mapToDTO(productRepository.save(product));
            auditLogService.log("Product.CREATE_SUCCESS | ProductID: " + result.getProductId()
                    + " | Name: " + result.getProductName()
                    + " | Category: " + result.getCategory()
                    + " | Price: " + result.getPrice()
                    + " | Status: ACTIVE");
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Product.CREATE", ex.getMessage());
            throw ex;
        }
    }

    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id));

        String before = "Name: " + product.getProductName()
                + " | Category: " + product.getCategory()
                + " | Price: " + product.getPrice()
                + " | Status: " + product.getStatus();

        try {
            product.setProductName(dto.getProductName());
            product.setCategory(dto.getCategory());
            product.setPrice(dto.getPrice());
            product.setStatus(dto.getStatus());
            ProductResponseDTO result = mapToDTO(productRepository.save(product));
            auditLogService.log("Product.UPDATE_SUCCESS | ProductID: " + id
                    + " | Before: " + before
                    + " | After: Name: " + dto.getProductName()
                    + " | Category: " + dto.getCategory()
                    + " | Price: " + dto.getPrice()
                    + " | Status: " + dto.getStatus());
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Product.UPDATE", ex.getMessage());
            throw ex;
        }
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id));
        try {
            // Soft delete the product
            product.setStatus("INACTIVE");
            productRepository.save(product);

            // Also deactivate all related catalog entries for this product
            List<Catalog> catalogs = catalogRepository.findByProductProductId(id);
            catalogs.forEach(c -> c.setStatus("INACTIVE"));
            catalogRepository.saveAll(catalogs);

            auditLogService.log("Product.DELETE_SUCCESS | ProductID: " + id
                    + " | Name: " + product.getProductName()
                    + " | Status: INACTIVE"
                    + " | CatalogsDeactivated: " + catalogs.size());
        } catch (Exception ex) {
            auditLogService.logFailure("Product.DELETE", ex.getMessage());
            throw ex;
        }
    }

    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public ProductResponseDTO getProductById(Long id) {
        return mapToDTO(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + id)));
    }

    public List<ProductResponseDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }


    private Product mapToEntity(ProductRequestDTO dto) {
        Product p = new Product();
        p.setProductName(dto.getProductName());
        p.setCategory(dto.getCategory());
        p.setPrice(dto.getPrice());
        p.setStatus(dto.getStatus());
        return p;
    }

    private ProductResponseDTO mapToDTO(Product p) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(p.getProductId());
        dto.setProductName(p.getProductName());
        dto.setCategory(p.getCategory());
        dto.setPrice(p.getPrice());
        dto.setStatus(p.getStatus());
        return dto;
    }
}