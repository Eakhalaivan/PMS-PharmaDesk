package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.ReturnToSupplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnToSupplierRepository extends JpaRepository<ReturnToSupplier, Long> {
    List<ReturnToSupplier> findBySupplierId(Long supplierId);
    List<ReturnToSupplier> findByGoodsReceiptNoteId(Long grnId);
}
