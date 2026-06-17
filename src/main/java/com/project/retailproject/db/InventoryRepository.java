package com.project.retailproject.db;

import com.project.retailproject.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByProductProductId(Long productId);
    List<Inventory> findByLocationId(Long locationId);
    List<Inventory> findByStatus(String status);
    Optional<Inventory> findByProductProductIdAndLocationId(Long productId, Long locationId);

    // for KPI computation
    @Query("SELECT COALESCE(AVG(i.quantityOnHand), 0) FROM Inventory i " +
            "WHERE i.status IN ('IN_STOCK', 'LOW_STOCK')")
    Double getAverageInventory();

    @Query("SELECT COALESCE(SUM(i.safetyStock), 0) FROM Inventory i " +
            "WHERE i.status IN ('IN_STOCK', 'LOW_STOCK')")
    Double getRecordedInventory();

    @Query("SELECT COALESCE(SUM(i.quantityOnHand), 0) FROM Inventory i " +
            "WHERE i.status IN ('IN_STOCK', 'LOW_STOCK')")
    Double getActualInventory();
}