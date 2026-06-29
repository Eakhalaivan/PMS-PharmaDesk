package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.MedicineStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineStockRepository extends JpaRepository<MedicineStock, Long> {

    @Query(value = "SELECT * FROM medicine_stocks WHERE id = :id AND is_deleted = false FOR UPDATE", nativeQuery = true)
    Optional<MedicineStock> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT s.medicine.id, SUM(s.quantityAvailable) FROM MedicineStock s GROUP BY s.medicine.id")
    List<Object[]> getStockQuantitiesGroupByMedicine();

    @Query("SELECT s.medicine.id, SUM(s.quantityAvailable) FROM MedicineStock s WHERE s.medicine.id IN :medicineIds GROUP BY s.medicine.id")
    List<Object[]> getStockQuantitiesGroupByMedicineIds(@Param("medicineIds") List<Long> medicineIds);

    @Query("SELECT s FROM MedicineStock s JOIN FETCH s.medicine LEFT JOIN FETCH s.supplier")
    List<MedicineStock> findAllWithMedicineAndSupplier();

    @Query("SELECT s FROM MedicineStock s JOIN FETCH s.medicine LEFT JOIN FETCH s.supplier WHERE LOWER(s.medicine.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<MedicineStock> findByMedicineNameContainingIgnoreCaseWithMedicineAndSupplier(@Param("name") String name);

    List<MedicineStock> findByMedicineNameContainingIgnoreCase(String name);
    Page<MedicineStock> findByMedicineNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);
    List<MedicineStock> findByMedicineId(Long medicineId);

    @Query("SELECT SUM(s.quantityAvailable) FROM MedicineStock s WHERE s.medicine.id = :medicineId AND s.deleted = false")
    Integer sumQuantityByMedicineId(@Param("medicineId") Long medicineId);

    @Query("SELECT SUM(s.quantityAvailable * s.sellingRate) FROM MedicineStock s")
    java.math.BigDecimal findTotalStockValue();

    @Query("SELECT SUM(s.purchaseRate * s.quantityAvailable) FROM MedicineStock s WHERE s.quantityAvailable > 0 AND s.deleted = false")
    BigDecimal getTotalPurchaseValue();

    @Query("SELECT SUM(s.sellingRate * s.quantityAvailable) FROM MedicineStock s WHERE s.quantityAvailable > 0 AND s.deleted = false")
    BigDecimal getTotalMrpValue();

    @Query("SELECT SUM(s.purchaseRate * s.quantityAvailable) FROM MedicineStock s WHERE s.quantityAvailable > 0 AND s.deleted = false AND s.expiryDate < CURRENT_DATE")
    BigDecimal getExpiredValue();

    @Query("SELECT SUM(s.purchaseRate * s.quantityAvailable) FROM MedicineStock s WHERE s.quantityAvailable > 0 AND s.deleted = false AND s.expiryDate >= CURRENT_DATE AND s.expiryDate <= :threshold")
    BigDecimal getNearExpiryValue(@Param("threshold") java.time.LocalDate threshold);

    @Query("SELECT s FROM MedicineStock s JOIN FETCH s.medicine LEFT JOIN FETCH s.supplier WHERE s.deleted = false AND s.quantityAvailable > 0")
    List<MedicineStock> findAllActiveWithMedicineAndSupplier();

    @Query("""
      SELECT m.name, m.category, SUM(ms.quantityAvailable), m.reorderLevel, m.unit
      FROM MedicineStock ms JOIN ms.medicine m
      WHERE ms.deleted = false AND ms.quantityAvailable > 0
      GROUP BY m.id, m.name, m.category, m.reorderLevel, m.unit
      HAVING SUM(ms.quantityAvailable) <= m.reorderLevel
      ORDER BY SUM(ms.quantityAvailable) ASC
    """)
    List<Object[]> findLowStockWithMedicine();

    @Query(value = """
      SELECT COUNT(*) FROM (
        SELECT m.id FROM medicine_stocks ms 
        JOIN medicines m ON ms.medicine_id = m.id
        WHERE ms.is_deleted = false
        GROUP BY m.id, m.reorder_level 
        HAVING SUM(ms.quantity_available) <= m.reorder_level
      ) AS low_stock
    """, nativeQuery = true)
    long countLowStockItems();

    List<MedicineStock> findByMedicineIdAndDeletedFalse(Long medicineId);
    
    @Query("SELECT s FROM MedicineStock s JOIN FETCH s.medicine " +
           "WHERE s.medicine.id IN :ids AND s.deleted = false " +
           "ORDER BY s.expiryDate ASC")
    List<MedicineStock> findByMedicineIdInAndDeletedFalseOrderByExpiryDateAsc(@Param("ids") List<Long> ids);
    
    @Query("SELECT ms.medicine.id, s.name FROM MedicineStock ms JOIN ms.supplier s WHERE ms.deleted = false AND ms.medicine.id IN :medicineIds GROUP BY ms.medicine.id, s.name")
    List<Object[]> findSupplierNamesByMedicineIds(@Param("medicineIds") List<Long> medicineIds);
    
    List<MedicineStock> findByExpiryDateBefore(java.time.LocalDate date);

    @Query("SELECT COUNT(DISTINCT s.medicine.id) FROM MedicineStock s WHERE s.deleted = false AND s.quantityAvailable > 0 AND s.expiryDate BETWEEN CURRENT_DATE AND :threshold")
    long countExpiringWithinDays(@Param("threshold") java.time.LocalDate threshold);

    default long countExpiringWithinDays(int days) {
        return countExpiringWithinDays(java.time.LocalDate.now().plusDays(days));
    }
}
