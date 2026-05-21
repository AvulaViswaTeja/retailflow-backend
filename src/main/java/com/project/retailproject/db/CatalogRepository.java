package com.project.retailproject.db;

import com.project.retailproject.model.Catalog;
import com.project.retailproject.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    List<Catalog> findByProductProductId(Long productId);
    List<Catalog> findByStatus(String status);

    Optional<Catalog> findFirstByProductAndStatusAndEffectiveDateLessThanEqualAndExpiryDateGreaterThanEqual(
            Product product,
            String status,
            LocalDate effectiveDate,
            LocalDate expiryDate);
}