package com.project.retailproject.db;

import com.project.retailproject.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProductProductId(Long productId);
    List<Inventory> findByLocationId(Long locationId);
    List<Inventory> findByStatus(String status);
    Optional<Inventory> findByProductProductIdAndLocationId(Long productId, Long locationId);
}