package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.TemperatureLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemperatureLogRepository extends JpaRepository<TemperatureLog, String> {
    List<TemperatureLog> findByStorageUnitUnitIdOrderByRecordedAtDesc(String unitId);
    List<TemperatureLog> findByBreachTrueOrderByRecordedAtDesc();
}
