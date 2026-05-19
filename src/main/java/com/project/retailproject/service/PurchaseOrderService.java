package com.project.retailproject.service;

import com.project.retailproject.db.ProductRepository;
import com.project.retailproject.db.PurchaseOrderRepository;
import com.project.retailproject.dto.PurchaseOrderRequestDTO;
import com.project.retailproject.dto.PurchaseOrderResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Product;
import com.project.retailproject.model.PurchaseOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    // Create PO
    public PurchaseOrderResponseDTO insertPurchaseOrder(PurchaseOrderRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + dto.getProductId()));

        if (dto.getExpectedDeliveryDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Expected delivery date cannot be in the past");
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplierId(dto.getSupplierId());
        po.setProduct(product);
        po.setOrderDate(LocalDate.now());
        po.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        po.setStatus("PENDING");

        return mapToDTO(purchaseOrderRepository.save(po));
    }

    // Update PO
    public PurchaseOrderResponseDTO updatePurchaseOrder(Long id, PurchaseOrderRequestDTO dto) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order not found with ID: " + id));

        if (po.getStatus().equals("DELIVERED")) {
            throw new BadRequestException("Cannot update a delivered purchase order");
        }

        po.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        po.setStatus(dto.getStatus());

        return mapToDTO(purchaseOrderRepository.save(po));
    }

    // Cancel PO (soft delete)
    public void deletePurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order not found with ID: " + id));
        po.setStatus("CANCELLED");
        purchaseOrderRepository.save(po);
    }

    // Get by ID
    public PurchaseOrderResponseDTO getPurchaseOrderById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order not found with ID: " + id));
        return mapToDTO(po);
    }

    // Get all
    public List<PurchaseOrderResponseDTO> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get by supplier
    public List<PurchaseOrderResponseDTO> getBySupplier(Long supplierId) {
        return purchaseOrderRepository.findBySupplierId(supplierId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get by status
    public List<PurchaseOrderResponseDTO> getByStatus(String status) {
        return purchaseOrderRepository.findByStatus(status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --- Mapper ---
    private PurchaseOrderResponseDTO mapToDTO(PurchaseOrder po) {
        PurchaseOrderResponseDTO dto = new PurchaseOrderResponseDTO();
        dto.setPurchaseOrderId(po.getPurchaseOrderId());
        dto.setSupplierId(po.getSupplierId());
        dto.setOrderDate(po.getOrderDate());
        dto.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        dto.setStatus(po.getStatus());
        if (po.getProduct() != null) {
            dto.setProductId(po.getProduct().getProductId());
            dto.setProductName(po.getProduct().getProductName());
        }
        return dto;
    }
}