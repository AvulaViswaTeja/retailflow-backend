package com.project.retailproject.service;

import com.project.retailproject.db.ProductRepository;
import com.project.retailproject.dto.ProductRequestDTO;
import com.project.retailproject.dto.ProductResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Add product
    public ProductResponseDTO addProduct(ProductRequestDTO dto) {
        // Check duplicate
        if (productRepository.existsByProductName(dto.getProductName())) {
            throw new BadRequestException("Product with name '" + dto.getProductName() + "' already exists");
        }
        Product product = mapToEntity(dto);
        product.setStatus("ACTIVE");
        return mapToDTO(productRepository.save(product));
    }

    // Get all products
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get by ID
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToDTO(product);
    }

    // Update product
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        product.setProductName(dto.getProductName());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setStatus(dto.getStatus());
        return mapToDTO(productRepository.save(product));
    }

    // Delete product (soft delete — sets status to INACTIVE)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        product.setStatus("INACTIVE");
        productRepository.save(product);
    }

    // Get by category
    public List<ProductResponseDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --- Mappers ---
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