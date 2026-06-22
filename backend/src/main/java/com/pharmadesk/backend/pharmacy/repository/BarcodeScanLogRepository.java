package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.BarcodeScanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarcodeScanLogRepository extends JpaRepository<BarcodeScanLog, String> {
}
