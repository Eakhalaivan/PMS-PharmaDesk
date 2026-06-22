package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.SupplierPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierPerformanceRepository extends JpaRepository<SupplierPerformance, Long> {
    List<SupplierPerformance> findBySupplierIdOrderByPeriodStartDesc(Long supplierId);
    Optional<SupplierPerformance> findTopBySupplierIdOrderByPeriodEndDesc(Long supplierId);
}
