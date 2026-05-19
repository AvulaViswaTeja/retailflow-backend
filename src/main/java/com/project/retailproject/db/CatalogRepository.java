package com.project.retailproject.db;

import com.project.retailproject.model.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    List<Catalog> findByProductProductId(Long productId);
    List<Catalog> findByStatus(String status);
}