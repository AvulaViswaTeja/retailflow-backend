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

    @Autowired
    private AuditLogService auditLogService;

    public PurchaseOrderResponseDTO insertPurchaseOrder(PurchaseOrderRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + dto.getProductId()));

        if (dto.getExpectedDeliveryDate().isBefore(LocalDate.now())) {
            auditLogService.logFailure("PurchaseOrder.CREATE",
                    "Past delivery date: " + dto.getExpectedDeliveryDate()
                            + " | ProductID: " + dto.getProductId());
            throw new BadRequestException("Expected delivery date cannot be in the past");
        }

        try {
            PurchaseOrder po = new PurchaseOrder();
            po.setSupplierId(dto.getSupplierId());
            po.setProduct(product);
            po.setOrderDate(LocalDate.now());
            po.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
            po.setStatus("PENDING");

            PurchaseOrderResponseDTO result = mapToDTO(purchaseOrderRepository.save(po));
            auditLogService.log("PurchaseOrder.CREATE_SUCCESS | POID: " + result.getPurchaseOrderId()
                    + " | SupplierID: " + dto.getSupplierId()
                    + " | ProductID: " + dto.getProductId()
                    + " | ExpectedDelivery: " + dto.getExpectedDeliveryDate()
                    + " | Status: PENDING");
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("PurchaseOrder.CREATE", ex.getMessage());
            throw ex;
        }
    }

    public PurchaseOrderResponseDTO updatePurchaseOrder(Long id, PurchaseOrderRequestDTO dto) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order not found with ID: " + id));

        if (po.getStatus().equals("DELIVERED")) {
            auditLogService.logFailure("PurchaseOrder.UPDATE",
                    "Attempt to update DELIVERED POID: " + id);
            throw new BadRequestException("Cannot update a delivered purchase order");
        }

        String before = "Status: " + po.getStatus()
                + " | ExpectedDelivery: " + po.getExpectedDeliveryDate();

        try {
            po.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
            po.setStatus(dto.getStatus());

            PurchaseOrderResponseDTO result = mapToDTO(purchaseOrderRepository.save(po));
            auditLogService.log("PurchaseOrder.UPDATE_SUCCESS | POID: " + id
                    + " | Before: " + before
                    + " | After: Status: " + dto.getStatus()
                    + " | ExpectedDelivery: " + dto.getExpectedDeliveryDate());
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("PurchaseOrder.UPDATE", ex.getMessage());
            throw ex;
        }
    }

    public void deletePurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order not found with ID: " + id));
        try {
            po.setStatus("CANCELLED");
            purchaseOrderRepository.save(po);
            auditLogService.log("PurchaseOrder.CANCEL_SUCCESS | POID: " + id
                    + " | SupplierID: " + po.getSupplierId()
                    + " | ProductID: " + po.getProduct().getProductId()
                    + " | Status: CANCELLED");
        } catch (Exception ex) {
            auditLogService.logFailure("PurchaseOrder.CANCEL", ex.getMessage());
            throw ex;
        }
    }

    public PurchaseOrderResponseDTO getPurchaseOrderById(Long id) {
        return mapToDTO(purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order not found with ID: " + id)));
    }

    public List<PurchaseOrderResponseDTO> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<PurchaseOrderResponseDTO> getBySupplier(Long supplierId) {
        return purchaseOrderRepository.findBySupplierId(supplierId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<PurchaseOrderResponseDTO> getByStatus(String status) {
        return purchaseOrderRepository.findByStatus(status)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

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