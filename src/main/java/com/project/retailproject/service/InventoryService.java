package com.project.retailproject.service;

import com.project.retailproject.db.InventoryRepository;
import com.project.retailproject.db.ProductRepository;
import com.project.retailproject.dto.InventoryRequestDTO;
import com.project.retailproject.dto.InventoryResponseDTO;
import com.project.retailproject.dto.PurchaseOrderResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Inventory;
import com.project.retailproject.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuditLogService auditLogService;

    public InventoryResponseDTO addInventory(InventoryRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + dto.getProductId()));

        inventoryRepository.findByProductProductIdAndLocationId(
                        dto.getProductId(), dto.getLocationId())
                .ifPresent(i -> {
                    auditLogService.logFailure("Inventory.CREATE",
                            "Duplicate inventory | ProductID: " + dto.getProductId()
                                    + " | LocationID: " + dto.getLocationId());
                    throw new BadRequestException(
                            "Inventory already exists for this product at this location");
                });

        try {
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setLocationId(dto.getLocationId());
            inventory.setQuantityOnHand(dto.getQuantityOnHand());
            inventory.setSafetyStock(dto.getSafetyStock());
            inventory.setStatus("ACTIVE");

            InventoryResponseDTO result = mapToDTO(inventoryRepository.save(inventory));
            auditLogService.log("Inventory.CREATE_SUCCESS | InventoryID: " + result.getInventoryId()
                    + " | ProductID: " + dto.getProductId()
                    + " | LocationID: " + dto.getLocationId()
                    + " | QtyOnHand: " + dto.getQuantityOnHand()
                    + " | SafetyStock: " + dto.getSafetyStock()
                    + " | Status: ACTIVE");
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Inventory.CREATE", ex.getMessage());
            throw ex;
        }
    }

    public InventoryResponseDTO updateInventory(Long id, InventoryRequestDTO dto) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with ID: " + id));

        String before = "QtyOnHand: " + inventory.getQuantityOnHand()
                + " | SafetyStock: " + inventory.getSafetyStock()
                + " | LocationID: " + inventory.getLocationId()
                + " | Status: " + inventory.getStatus();

        try {
            inventory.setQuantityOnHand(dto.getQuantityOnHand());
            inventory.setSafetyStock(dto.getSafetyStock());
            inventory.setLocationId(dto.getLocationId());
            inventory.setStatus(dto.getStatus());

            InventoryResponseDTO result = mapToDTO(inventoryRepository.save(inventory));
            auditLogService.log("Inventory.UPDATE_SUCCESS | InventoryID: " + id
                    + " | Before: " + before
                    + " | After: QtyOnHand: " + dto.getQuantityOnHand()
                    + " | SafetyStock: " + dto.getSafetyStock()
                    + " | LocationID: " + dto.getLocationId()
                    + " | Status: " + dto.getStatus());
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Inventory.UPDATE", ex.getMessage());
            throw ex;
        }
    }

    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with ID: " + id));
        try {
            inventory.setStatus("INACTIVE");
            inventoryRepository.save(inventory);
            auditLogService.log("Inventory.DELETE_SUCCESS | InventoryID: " + id
                    + " | ProductID: " + inventory.getProduct().getProductId()
                    + " | Status: INACTIVE");
        } catch (Exception ex) {
            auditLogService.logFailure("Inventory.DELETE", ex.getMessage());
            throw ex;
        }
    }

    public InventoryResponseDTO replenishStock(Long id, Integer quantity) {
        if (quantity <= 0) {
            auditLogService.logFailure("Inventory.REPLENISH",
                    "Invalid quantity: " + quantity + " | InventoryID: " + id);
            throw new BadRequestException("Quantity must be greater than 0");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with ID: " + id));

        int oldQty = inventory.getQuantityOnHand();
        try {
            inventory.setQuantityOnHand(oldQty + quantity);
            InventoryResponseDTO result = mapToDTO(inventoryRepository.save(inventory));
            auditLogService.log("Inventory.REPLENISH_SUCCESS | InventoryID: " + id
                    + " | ProductID: " + inventory.getProduct().getProductId()
                    + " | QtyBefore: " + oldQty
                    + " | Added: " + quantity
                    + " | QtyAfter: " + inventory.getQuantityOnHand());
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Inventory.REPLENISH", ex.getMessage());
            throw ex;
        }
    }

    public InventoryResponseDTO getInventoryById(Long id) {
        return mapToDTO(inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with ID: " + id)));
    }

    public List<InventoryResponseDTO> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<InventoryResponseDTO> getInventoryByProduct(Long productId) {
        return inventoryRepository.findByProductProductId(productId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<InventoryResponseDTO> getLowStockInventory() {
        return inventoryRepository.findAll().stream()
                .filter(i -> i.getQuantityOnHand() != null
                        && i.getSafetyStock() != null
                        && i.getQuantityOnHand() < i.getSafetyStock())
                .map(this::mapToDTO).collect(Collectors.toList());
    }
    public Page<InventoryResponseDTO> getAllInventoryWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return inventoryRepository.findAll(pageable).map(this::mapToDTO);
    }

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
        if (i.getQuantityOnHand() != null && i.getSafetyStock() != null) {
            dto.setLowStock(i.getQuantityOnHand() < i.getSafetyStock());
        }
        return dto;
    }
}