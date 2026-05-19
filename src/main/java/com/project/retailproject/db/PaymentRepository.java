package com.project.retailproject.db;

import com.project.retailproject.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {

    List<Payment> findByInvoice_InvoiceId(Long invoiceId);

    List<Payment> findByStatus(String status);

    List<Payment> findByMethod(String method);

    List<Payment> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.invoice.invoiceId = :invoiceId AND p.status = 'SUCCESS'")
    Double sumSuccessfulPaymentsByInvoiceId(@Param("invoiceId") Long invoiceId);
}
