package com.project.retailproject.service;

import com.project.retailproject.db.InvoiceRepository;
import com.project.retailproject.db.PaymentRepository;
import com.project.retailproject.dto.PaymentRequestDTO;
import com.project.retailproject.dto.PaymentResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Invoice;
import com.project.retailproject.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Transactional
    public PaymentResponseDTO insertPayment(PaymentRequestDTO dto) {
        Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + dto.getInvoiceId()));

        if (invoice.getStatus().equals("CANCELLED")) {
            throw new BadRequestException("Cannot pay a cancelled invoice");
        }
        if (invoice.getStatus().equals("PAID")) {
            throw new BadRequestException("Invoice is already fully paid");
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(dto.getAmount());
        payment.setMethod(dto.getMethod());
        payment.setDate(LocalDate.now());
        payment.setStatus("SUCCESS");

        Payment saved = paymentRepository.save(payment);

        // Update invoice status based on total paid
        Double totalPaid = paymentRepository
                .sumSuccessfulPaymentsByInvoiceId(invoice.getInvoiceId());

        if (totalPaid == null) totalPaid = 0.0;

        if (totalPaid >= invoice.getAmount()) {
            invoice.setStatus("PAID");
        } else if (totalPaid > 0) {
            invoice.setStatus("PARTIALLY_PAID");
        }

        invoiceRepository.save(invoice);

        return mapToDTO(saved, invoice.getStatus());
    }

    public PaymentResponseDTO updatePayment(Long id, PaymentRequestDTO dto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with ID: " + id));
        payment.setMethod(dto.getMethod());
        return mapToDTO(paymentRepository.save(payment), null);
    }

    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with ID: " + id));
        payment.setStatus("REFUNDED");
        paymentRepository.save(payment);
    }

    public PaymentResponseDTO getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with ID: " + id));
        return mapToDTO(payment, null);
    }

    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream().map(p -> mapToDTO(p, null))
                .collect(Collectors.toList());
    }

    public List<PaymentResponseDTO> getByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoice_InvoiceId(invoiceId)
                .stream().map(p -> mapToDTO(p, null))
                .collect(Collectors.toList());
    }

    public Page<PaymentResponseDTO> getAllPaymentsPaginated(Pageable pageable) {
        return paymentRepository.findAll(pageable)
                .map(p -> mapToDTO(p, null));
    }

    // --- Mapper ---
    private PaymentResponseDTO mapToDTO(Payment p, String invoiceStatus) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setPaymentId(p.getPaymentId());
        dto.setAmount(p.getAmount());
        dto.setDate(p.getDate());
        dto.setMethod(p.getMethod());
        dto.setStatus(p.getStatus());
        dto.setInvoiceStatus(invoiceStatus);
        if (p.getInvoice() != null) {
            dto.setInvoiceId(p.getInvoice().getInvoiceId());
        }
        return dto;
    }
}