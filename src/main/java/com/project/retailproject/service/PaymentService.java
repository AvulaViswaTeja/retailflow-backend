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

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public PaymentResponseDTO insertPayment(PaymentRequestDTO dto) {
        Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with ID: " + dto.getInvoiceId()));

        if (invoice.getStatus().equals("CANCELLED")) {
            auditLogService.logFailure("Payment.PROCESS",
                    "Attempt to pay CANCELLED InvoiceID: " + dto.getInvoiceId());
            throw new BadRequestException("Cannot pay a cancelled invoice");
        }
        if (invoice.getStatus().equals("PAID")) {
            auditLogService.logFailure("Payment.PROCESS",
                    "Attempt to pay already PAID InvoiceID: " + dto.getInvoiceId());
            throw new BadRequestException("Invoice is already fully paid");
        }

        try {
            Payment payment = new Payment();
            payment.setInvoice(invoice);
            payment.setAmount(dto.getAmount());
            payment.setMethod(dto.getMethod());
            payment.setDate(LocalDate.now());
            payment.setStatus("SUCCESS");

            Payment saved = paymentRepository.save(payment);

            Double totalPaid = paymentRepository
                    .sumSuccessfulPaymentsByInvoiceId(invoice.getInvoiceId());
            if (totalPaid == null) totalPaid = 0.0;

            String newInvoiceStatus;
            if (totalPaid >= invoice.getAmount()) {
                newInvoiceStatus = "PAID";
            } else if (totalPaid > 0) {
                newInvoiceStatus = "PARTIALLY_PAID";
            } else {
                newInvoiceStatus = invoice.getStatus();
            }

            invoice.setStatus(newInvoiceStatus);
            invoiceRepository.save(invoice);

            auditLogService.log("Payment.PROCESS_SUCCESS | PaymentID: " + saved.getPaymentId()
                    + " | InvoiceID: " + dto.getInvoiceId()
                    + " | Amount: " + dto.getAmount()
                    + " | Method: " + dto.getMethod()
                    + " | TotalPaid: " + totalPaid
                    + " | InvoiceStatus: " + newInvoiceStatus);

            return mapToDTO(saved, newInvoiceStatus);
        } catch (Exception ex) {
            auditLogService.logFailure("Payment.PROCESS", ex.getMessage());
            throw ex;
        }
    }

    public PaymentResponseDTO updatePayment(Long id, PaymentRequestDTO dto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with ID: " + id));

        String oldMethod = payment.getMethod();
        try {
            payment.setMethod(dto.getMethod());
            PaymentResponseDTO result = mapToDTO(paymentRepository.save(payment), null);
            auditLogService.log("Payment.UPDATE_SUCCESS | PaymentID: " + id
                    + " | Method: " + oldMethod + " -> " + dto.getMethod());
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("Payment.UPDATE", ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public void refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with ID: " + id));

        if (payment.getStatus().equals("REFUNDED")) {
            throw new BadRequestException("Payment is already refunded");
        }

        try {
            payment.setStatus("REFUNDED");
            paymentRepository.save(payment);


            Invoice invoice = payment.getInvoice();
            Double totalPaid = paymentRepository
                    .sumSuccessfulPaymentsByInvoiceId(invoice.getInvoiceId());
            if (totalPaid == null) totalPaid = 0.0;

            String newInvoiceStatus;
            if (totalPaid <= 0) {
                newInvoiceStatus = "PENDING";
            } else if (totalPaid < invoice.getAmount()) {
                newInvoiceStatus = "PARTIALLY_PAID";
            } else {
                newInvoiceStatus = "PAID";
            }

            invoice.setStatus(newInvoiceStatus);
            invoiceRepository.save(invoice);

            auditLogService.log("Payment.REFUND_SUCCESS | PaymentID: " + id
                    + " | InvoiceID: " + invoice.getInvoiceId()
                    + " | Amount: " + payment.getAmount()
                    + " | Status: REFUNDED"
                    + " | InvoiceStatus: " + newInvoiceStatus);

        } catch (Exception ex) {
            auditLogService.logFailure("Payment.REFUND", ex.getMessage());
            throw ex;
        }
    }

    public PaymentResponseDTO getPayment(Long id) {
        return mapToDTO(paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with ID: " + id)), null);
    }

    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(p -> mapToDTO(p, null)).collect(Collectors.toList());
    }

    public List<PaymentResponseDTO> getByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoice_InvoiceId(invoiceId)
                .stream().map(p -> mapToDTO(p, null)).collect(Collectors.toList());
    }

    public Page<PaymentResponseDTO> getAllPaymentsPaginated(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(p -> mapToDTO(p, null));
    }


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