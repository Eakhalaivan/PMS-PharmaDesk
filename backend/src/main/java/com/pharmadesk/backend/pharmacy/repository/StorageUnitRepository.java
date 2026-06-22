package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.StorageUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageUnitRepository extends JpaRepository<StorageUnit, String> {
}
