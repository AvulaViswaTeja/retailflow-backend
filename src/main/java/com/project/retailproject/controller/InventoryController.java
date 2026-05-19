package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.InventoryRequestDTO;
import com.project.retailproject.dto.InventoryResponseDTO;
import com.project.retailproject.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> addInventory(
            @Valid @RequestBody InventoryRequestDTO dto) {
        InventoryResponseDTO data = inventoryService.addInventory(dto);
        return ResponseEntity.ok(ApiResponse.success("Inventory added successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponseDTO>>> getAllInventory() {
        List<InventoryResponseDTO> data = inventoryService.getAllInventory();
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> getInventoryById(
            @PathVariable Long id) {
        InventoryResponseDTO data = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequestDTO dto) {
        InventoryResponseDTO data = inventoryService.updateInventory(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Inventory updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory deactivated successfully", null));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryResponseDTO>>> getByProduct(
            @PathVariable Long productId) {
        List<InventoryResponseDTO> data = inventoryService.getInventoryByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved successfully", data));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryResponseDTO>>> getLowStock() {
        List<InventoryResponseDTO> data = inventoryService.getLowStockInventory();
        return ResponseEntity.ok(ApiResponse.success("Low stock items retrieved", data));
    }

    @PatchMapping("/{id}/replenish")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> replenishStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        InventoryResponseDTO data = inventoryService.replenishStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock replenished successfully", data));
    }
}