package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.PurchaseOrderRequestDTO;
import com.project.retailproject.dto.PurchaseOrderResponseDTO;
import com.project.retailproject.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderRequestDTO dto) {
        PurchaseOrderResponseDTO data = purchaseOrderService.insertPurchaseOrder(dto);
        return ResponseEntity.ok(ApiResponse.success("Purchase order created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponseDTO>>> getAllPurchaseOrders() {
        List<PurchaseOrderResponseDTO> data = purchaseOrderService.getAllPurchaseOrders();
        return ResponseEntity.ok(ApiResponse.success("Purchase orders retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> getPurchaseOrderById(
            @PathVariable Long id) {
        PurchaseOrderResponseDTO data = purchaseOrderService.getPurchaseOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order retrieved successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> updatePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderRequestDTO dto) {
        PurchaseOrderResponseDTO data = purchaseOrderService.updatePurchaseOrder(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Purchase order updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelPurchaseOrder(@PathVariable Long id) {
        purchaseOrderService.deletePurchaseOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order cancelled successfully", null));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponseDTO>>> getBySupplier(
            @PathVariable Long supplierId) {
        List<PurchaseOrderResponseDTO> data = purchaseOrderService.getBySupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.success("Purchase orders retrieved successfully", data));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponseDTO>>> getByStatus(
            @PathVariable String status) {
        List<PurchaseOrderResponseDTO> data = purchaseOrderService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Purchase orders retrieved successfully", data));
    }
}