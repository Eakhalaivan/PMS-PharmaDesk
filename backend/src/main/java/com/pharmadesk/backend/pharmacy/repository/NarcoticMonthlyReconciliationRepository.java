package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.NarcoticMonthlyReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NarcoticMonthlyReconciliationRepository extends JpaRepository<NarcoticMonthlyReconciliation, String> {
    Optional<NarcoticMonthlyReconciliation> findByMedicineIdAndReconciliationMonthAndReconciliationYear(Long medicineId, int month, int year);
}
