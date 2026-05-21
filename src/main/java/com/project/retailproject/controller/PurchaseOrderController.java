package com.project.retailproject.controller;

import com.project.retailproject.dto.PurchaseOrderRequestDTO;
import com.project.retailproject.dto.PurchaseOrderResponseDTO;
import com.project.retailproject.service.PurchaseOrderService;
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
    public ResponseEntity<PurchaseOrderResponseDTO> createPO(
            @RequestBody PurchaseOrderRequestDTO dto) {
        return ResponseEntity.ok(purchaseOrderService.insertPurchaseOrder(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDTO> updatePO(
            @PathVariable Long id, @RequestBody PurchaseOrderRequestDTO dto) {
        return ResponseEntity.ok(purchaseOrderService.updatePurchaseOrder(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelPO(@PathVariable Long id) {
        purchaseOrderService.deletePurchaseOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDTO> getPO(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getAllPOs() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getBySupplier(
            @PathVariable Long supplierId) {
        return ResponseEntity.ok(purchaseOrderService.getBySupplier(supplierId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(purchaseOrderService.getByStatus(status));
    }
}