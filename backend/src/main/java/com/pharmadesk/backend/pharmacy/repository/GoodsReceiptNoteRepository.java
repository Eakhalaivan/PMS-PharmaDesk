package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.GoodsReceiptNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceiptNoteRepository extends JpaRepository<GoodsReceiptNote, Long> {
    Optional<GoodsReceiptNote> findByGrnNumber(String grnNumber);
    List<GoodsReceiptNote> findByPurchaseOrderPoId(String poId);
    List<GoodsReceiptNote> findBySupplierId(Long supplierId);

    @Query("SELECT COUNT(g) FROM GoodsReceiptNote g WHERE g.supplier.id = :supplierId AND g.deleted = false")
    long countBySupplierId(Long supplierId);
}
