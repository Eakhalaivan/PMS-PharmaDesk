package com.pharmadesk.backend.pharmacy.repository;

import com.pharmadesk.backend.model.DrugInteractionCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrugInteractionCheckRepository extends JpaRepository<DrugInteractionCheck, String> {
}
