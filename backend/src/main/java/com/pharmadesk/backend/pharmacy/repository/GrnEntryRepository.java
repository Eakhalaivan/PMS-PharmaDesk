package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.GrnEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrnEntryRepository extends JpaRepository<GrnEntry, String> {
    List<GrnEntry> findBySupplierId(Long supplierId);
    List<GrnEntry> findByPurchaseOrderPoId(String poId);
}
