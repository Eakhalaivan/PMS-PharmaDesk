package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {

    List<PurchaseOrder> findByStatus(String status);

    List<PurchaseOrder> findBySupplierIdAndStatus(Long supplierId, String status);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.expectedDeliveryDate < :today AND po.status NOT IN ('completed', 'cancelled')")
    List<PurchaseOrder> findOverduePOs(@Param("today") java.time.LocalDate today);

    long countByStatus(String status);

    org.springframework.data.domain.Page<PurchaseOrder> findByStatus(
            String status, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<PurchaseOrder> findByPoNumberContainingIgnoreCaseOrSupplierNameContainingIgnoreCase(
            String poNumber, String supplierName,
            org.springframework.data.domain.Pageable pageable);
}
