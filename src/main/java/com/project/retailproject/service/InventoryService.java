package com.project.retailproject.service;

import com.project.retailproject.db.InventoryRepository;
import com.project.retailproject.db.ProductRepository;
import com.project.retailproject.dto.InventoryRequestDTO;
import com.project.retailproject.dto.InventoryResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Inventory;
import com.project.retailproject.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // Add inventory
    public InventoryResponseDTO addInventory(InventoryRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + dto.getProductId()));

        // Check if inventory already exists for this product + location
        inventoryRepository.findByProductProductIdAndLocationId(
                        dto.getProductId(), dto.getLocationId())
                .ifPresent(i -> { throw new BadRequestException(
                        "Inventory already exists for this product at this location"); });

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setLocationId(dto.getLocationId());
        inventory.setQuantityOnHand(dto.getQuantityOnHand());
        inventory.setSafetyStock(dto.getSafetyStock());
        inventory.setStatus("ACTIVE");

        return mapToDTO(inventoryRepository.save(inventory));
    }

    // Update inventory
    public InventoryResponseDTO updateInventory(Long id, InventoryRequestDTO dto) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with ID: " + id));

        inventory.setQuantityOnHand(dto.getQuantityOnHand());
        inventory.setSafetyStock(dto.getSafetyStock());
        inventory.setLocationId(dto.getLocationId());
        inventory.setStatus(dto.getStatus());

        return mapToDTO(inventoryRepository.save(inventory));
    }

    // Soft delete
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with ID: " + id));
        inventory.setStatus("INACTIVE");
        inventoryRepository.save(inventory);
    }

    // Get by ID
    public InventoryResponseDTO getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with ID: " + id));
        return mapToDTO(inventory);
    }

    // Get all
    public List<InventoryResponseDTO> getAllInventory() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get by product
    public List<InventoryResponseDTO> getInventoryByProduct(Long productId) {
        return inventoryRepository.findByProductProductId(productId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get low stock items (quantity below safety stock)
    public List<InventoryResponseDTO> getLowStockInventory() {
        return inventoryRepository.findAll()
                .stream()
                .filter(i -> i.getQuantityOnHand() != null
                        && i.getSafetyStock() != null
                        && i.getQuantityOnHand() < i.getSafetyStock())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Replenish stock
    public InventoryResponseDTO replenishStock(Long id, Integer quantity) {
        if (quantity <= 0) throw new BadRequestException("Quantity must be greater than 0");
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with ID: " + id));
        inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantity);
        return mapToDTO(inventoryRepository.save(inventory));
    }

    // --- Mapper ---
    private InventoryResponseDTO mapToDTO(Inventory i) {
        InventoryResponseDTO dto = new InventoryResponseDTO();
        dto.setInventoryId(i.getInventoryId());
        dto.setLocationId(i.getLocationId());
        dto.setQuantityOnHand(i.getQuantityOnHand());
        dto.setSafetyStock(i.getSafetyStock());
        dto.setStatus(i.getStatus());
        if (i.getProduct() != null) {
            dto.setProductId(i.getProduct().getProductId());
            dto.setProductName(i.getProduct().getProductName());
        }
        // Flag if quantity is below safety stock
        if (i.getQuantityOnHand() != null && i.getSafetyStock() != null) {
            dto.setLowStock(i.getQuantityOnHand() < i.getSafetyStock());
        }
        return dto;
    }
}