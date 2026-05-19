package com.project.retailproject.db;

import com.project.retailproject.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {


    List<Sale> findByCustomerId(Long customerId);

    List<Sale> findByStatus(String status);

    List<Sale> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Sale> findByProduct_ProductId(Long productId);

    long countByCustomerId(Long customerId);
}