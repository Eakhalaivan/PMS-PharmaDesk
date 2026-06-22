package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.InsuranceMedicineCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceMedicineCoverageRepository extends JpaRepository<InsuranceMedicineCoverage, String> {
    List<InsuranceMedicineCoverage> findByProviderProviderId(String providerId);
    Optional<InsuranceMedicineCoverage> findByProviderProviderIdAndMedicineId(String providerId, Long medicineId);
}
