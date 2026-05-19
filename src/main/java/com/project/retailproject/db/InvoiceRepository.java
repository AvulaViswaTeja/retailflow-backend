package com.project.retailproject.db;

import com.project.retailproject.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {


    Optional<Invoice> findBySale_SaleId(Long saleId);

    List<Invoice> findByStatus(String status);

    List<Invoice> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Invoice> findBySale_CustomerIdAndStatus(Long customerId, String status);

    long countByStatus(String status);
}