package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.PaymentRequestDTO;
import com.project.retailproject.dto.PaymentResponseDTO;
import com.project.retailproject.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> insertPayment(
            @Valid @RequestBody PaymentRequestDTO dto) {
        PaymentResponseDTO data = paymentService.insertPayment(dto);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> getAllPayments() {
        List<PaymentResponseDTO> data = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getPayment(@PathVariable Long id) {
        PaymentResponseDTO data = paymentService.getPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequestDTO dto) {
        PaymentResponseDTO data = paymentService.updatePayment(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Payment updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", null));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> getByInvoice(
            @PathVariable Long invoiceId) {
        List<PaymentResponseDTO> data = paymentService.getByInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", data));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<PaymentResponseDTO>>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentId") String sorting,
            @RequestParam(defaultValue = "true") boolean asc) {
        Sort sort = asc ? Sort.by(sorting).ascending() : Sort.by(sorting).descending();
        Page<PaymentResponseDTO> data = paymentService.getAllPaymentsPaginated(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", data));
    }
}