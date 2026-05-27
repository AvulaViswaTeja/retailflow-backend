package com.project.retailproject.db;

import com.project.retailproject.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // existing methods — unchanged
    List<Inventory> findByProductProductId(Long productId);
    List<Inventory> findByLocationId(Long locationId);
    List<Inventory> findByStatus(String status);
    Optional<Inventory> findByProductProductIdAndLocationId(Long productId, Long locationId);

    // NEW — added for KPI computation
    @Query("SELECT COALESCE(AVG(i.quantityOnHand), 0) FROM Inventory i " +
            "WHERE i.status = 'ACTIVE'")
    Double getAverageInventory();

    @Query("SELECT COALESCE(SUM(i.safetyStock), 0) FROM Inventory i " +
            "WHERE i.status = 'ACTIVE'")
    Double getRecordedInventory();

    @Query("SELECT COALESCE(SUM(i.quantityOnHand), 0) FROM Inventory i " +
            "WHERE i.status = 'ACTIVE'")
    Double getActualInventory();
}