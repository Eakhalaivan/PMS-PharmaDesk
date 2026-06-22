package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.SupplierInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, Long> {
    List<SupplierInvoice> findBySupplierId(Long supplierId);
    List<SupplierInvoice> findBySupplierIdAndStatus(Long supplierId, String status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM SupplierInvoice i WHERE i.supplier.id = :supplierId AND i.status NOT IN ('PAID') AND i.deleted = false")
    BigDecimal sumOutstandingBySupplierId(Long supplierId);
}
